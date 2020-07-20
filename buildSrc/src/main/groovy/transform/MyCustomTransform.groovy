package transform

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import groovy.io.FileType
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.ClassWriter
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.utils.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class MyTransform extends Transform {

    /**
     * 每一个Transform都有一个与之对应的Transform task
     * 这里便是返回的task name,它会出现在app/build/intermediates/transforms目录下面
     */
    @Override
    String getName() {
        return "MyCustomTransform"
    }

    /**
     * 需要处理的数据类型
     * 有六种枚举类型,使用比较频繁的是前2种:
     *       1.CONTENT_CLASS：表示需要处理 java 的 class 文件      
     *      2.CONTENT_JARS：表示需要处理 java 的 class 与 资源文件  
     *      3.CONTENT_RESOURCES：表示需要处理 java 的资源文件 
     *      4.CONTENT_NATIVE_LIBS：表示需要处理 native 库的代码 
     *      5.CONTENT_DEX：表示需要处理 DEX 文件 
     *      6.CONTENT_DEX_WITH_RESOURCES：表示需要处理 DEX 与 java 的资源文件 
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        //用于确定我们需要对哪些类型的结果进行转换:如字节码,资源文件等
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 表示 Transform 要操作的内容范围，目前 Scope 有五种基本类型：       
     * 1.PROJECT                   只有项目内容       
     * 2.SUB_PROJECTS              只有子项目      
     * 3.EXTERNAL_LIBRARIES        只有外部库      
     * 4.TESTED_CODE               由当前变体（包括依赖项）所测试的代码      
     * 5.PROVIDED_ONLY             只提供本地或远程依赖项     
     * SCOPE_FULL_PROJECT 是一个 Scope 集合，包含 Scope.PROJECT,Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES 这三项，即当前 Transform的作用域包括当前项目.子项目以及外部的依赖库
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        //适用范围:  通常是指定整个project，也可以指定其他范围
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        //是否支持增量更新
        //如果返回true,TransformInput会包含一份修改的文件列表
        //如果返回false,会进行全量编译,删除上一次的输出内容
        return false
    }

    /**
     * 进行具体转换
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println '--------------- MyTransform visit start --------------- '
        def startTime = System.currentTimeMillis()
        def inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider
        //删除之前的输出
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }

        //Transform的inputs有两种类型,一种是目录,一种是jar包
        inputs.each { TransformInput input ->
            //遍历directoryInputs (本地project编译成的多个class文件存放的目录)
            input.directoryInputs.each { DirectoryInput directoryInput ->
                handleDirectory(directoryInput, outputProvider)
            }

            //遍历jarInputs(各个依赖所编译成的jar文件)
            input.jarInputs.each { JarInput jarInput ->
                handleJar(jarInput, outputProvider)
            }
        }

        def cost = (System.currentTimeMillis() - startTime) / 1000
        println '--------------- MyTransform visit end --------------- '
        println "MyTransform cost ： $cost s"
    }

    static void handleJar(JarInput jarInput, TransformOutputProvider outputProvider) {
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            // 截取文件路径的 md5 值重命名输出文件，避免出现同名而覆盖的情况出现
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            // 避免上次的缓存被重复插入
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                if (checkClassFile(entryName)) {
                    // 使用 ASM 对 class 文件进行操控
                    println '----------- deal with "jar" class file <' + entryName + '> -----------'
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, org.objectweb.asm.ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new MyCustomClassVisitor(classWriter)
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()

            // 生成输出路径 dest：./app/build/intermediates/transforms/xxxTransform/...
            def dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.contentTypes, jarInput.scopes, Format.JAR)
            // 将 input 的目录复制到 output 指定目录
            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
        }
    }

    static void handleDirectory(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        // 在增量模式下可以通过 directoryInput.changedFiles 方法获取修改的文件
//        directoryInput.changedFiles
        if (directoryInput.file.size() == 0)
            return
        if (directoryInput.file.isDirectory()) {
            /**遍历以某一扩展名结尾的文件*/
            directoryInput.file.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                File classFile ->
                    def name = classFile.name
                    if (checkClassFile(name)) {
                        println '----------- deal with "class" file <' + name + '> -----------'
                        def classReader = new ClassReader(classFile.bytes)
                        def classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                        def classVisitor = new MyCustomClassVisitor(classWriter)
                        classReader.accept(classVisitor, EXPAND_FRAMES)
                        byte[] codeBytes = classWriter.toByteArray()
                        FileOutputStream fileOutputStream = new FileOutputStream(
                                classFile.parentFile.absolutePath + File.separator + name
                        )
                        fileOutputStream.write(codeBytes)
                        fileOutputStream.close()
                    }
            }
        }
        /// 获取 output 目录 dest：./app/build/intermediates/transforms/hencoderTransform/
        def destFile = outputProvider.getContentLocation(
                directoryInput.name,
                directoryInput.contentTypes,
                directoryInput.scopes,
                Format.DIRECTORY
        )
        // 将 input 的目录复制到 output 指定目录
        FileUtils.copyDirectory(directoryInput.file, destFile)
    }

    /**
     * 检查 class 文件是否需要处理
     *
     * @param fileName
     * @return class 文件是否需要处理
     */
    static boolean checkClassFile(String name) {
        // 只处理需要的 class 文件
        return (name.endsWith(".class") && !name.startsWith("R\$")
                && "R.class" != name && "BuildConfig.class" != name
                && "android/support/v4/app/FragmentActivity.class" == name)
    }


}