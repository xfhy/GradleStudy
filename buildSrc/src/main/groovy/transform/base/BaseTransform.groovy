package transform.base

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.concurrent.Callable


abstract class BaseTransform extends Transform {
    protected WaitableExecutor mWaitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()

    @Override
    String getName() {
        return "MethodTimeTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        //需要处理的数据类型,这里表示class文件
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        //作用范围
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        //是否支持增量编译
        return true
    }

    /**
     * 关键: 在transform方法中，我们需要将每个jar包和class文件复制到dest路径，这个dest路径就是下一个Transform的输入数据。
     * 而在复制时，就可以将jar包和class文件的字节码做一些修改，再进行复制。
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        printCopyRight()

        //TransformOutputProvider管理输出路径,如果消费型输入为空,则outputProvider也为空
        TransformOutputProvider outputProvider = transformInvocation.outputProvider

        //当前是否是增量编译,由isIncremental方法决定的
        // 当上面的isIncremental()写的返回true,这里得到的值不一定是true,还得看当时环境.比如clean之后第一次运行肯定就不是增量编译嘛.
        boolean isIncremental = transformInvocation.isIncremental()
        if (!isIncremental) {
            //不是增量编译则删除之前的所有文件
            outputProvider.deleteAll()
        }

        //transformInvocation.inputs的类型是Collection<TransformInput>,可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        transformInvocation.inputs.each { input -> //这里的input是TransformInput

            input.jarInputs.each { jarInput ->
                //处理jar
                mWaitableExecutor.execute(new Callable<Object>() {
                    @Override
                    Object call() throws Exception {
                        //多线程
                        processJarInput(jarInput, outputProvider, isIncremental)
                        return null
                    }
                })
            }

            //处理源码文件
            input.directoryInputs.each { directoryInput ->
                //多线程
                mWaitableExecutor.execute(new Callable<Object>() {
                    @Override
                    Object call() throws Exception {
                        processDirectoryInput(directoryInput, outputProvider, isIncremental)
                        return null
                    }
                })
            }
        }

        //等待所有任务结束
        mWaitableExecutor.waitForTasksWithQuickFail(true)
    }

    /**
     * 处理jar
     * 将修改过的字节码copy到dest,就可以实现编译期间干预字节码的目的
     */
    void processJarInput(JarInput jarInput, TransformOutputProvider outputProvider, boolean isIncremental) {
        def status = jarInput.status
        File dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (isIncremental) {
            switch (status) {
                case Status.NOTCHANGED:
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    transformJar(jarInput.file, dest)
                    break
                case Status.REMOVED:
                    if (dest.exists()) {
                        FileUtils.forceDelete(dest)
                    }
                    break
            }
        } else {
            transformJar(jarInput.file, dest)
        }

    }

    void transformJar(File jarInputFile, File dest) {
        //println("拷贝文件 $dest -----")
        FileUtils.copyFile(jarInputFile, dest)
    }

    /**
     * 处理源码文件
     * 将修改过的字节码copy到dest,就可以实现编译期间干预字节码的目的
     */
    void processDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider, boolean isIncremental) {
        File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format
                .DIRECTORY)
        FileUtils.forceMkdir(dest)

        println("isIncremental = $isIncremental")

        if (isIncremental) {
            String srcDirPath = directoryInput.getFile().getAbsolutePath()
            String destDirPath = dest.getAbsolutePath()
            Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
            for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
                Status status = changedFile.getValue()
                File inputFile = changedFile.getKey()
                String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath)
                File destFile = new File(destFilePath)
                switch (status) {
                    case Status.NOTCHANGED:
                        break
                    case Status.ADDED:
                    case Status.CHANGED:
                        FileUtils.touch(destFile)
                        transformSingleFile(inputFile, destFile)
                        break
                    case Status.REMOVED:
                        if (destFile.exists()) {
                            FileUtils.forceDelete(destFile)
                        }
                        break
                }
            }
        } else {
            transformDirectory(directoryInput, dest)
        }
    }

    void transformSingleFile(File inputFile, File destFile) {
        println("拷贝单个文件")
        //FileUtils.copyFile(inputFile, destFile)
        traceFile(inputFile, destFile)
    }

    void traceFile(File inputFile, File outputFile) {
        if (isNeedTraceClass(inputFile)) {
            println("${inputFile.name} ---- 需要插桩 ----")
            FileInputStream inputStream = new FileInputStream(inputFile)
            FileOutputStream outputStream = new FileOutputStream(outputFile)

            ClassReader classReader = new ClassReader(inputStream)
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
            classReader.accept(getClassVisitor(classWriter), ClassReader.EXPAND_FRAMES)
            outputStream.write(classWriter.toByteArray())

            inputStream.close()
            outputStream.close()
        } else {
            FileUtils.copyFile(inputFile, outputFile)
        }
    }

    /**
     * 获取一个用于插桩的ClassVisitor
     */
    abstract ClassVisitor getClassVisitor(ClassWriter classWriter)

    /**
     * 这个文件是否需要插桩
     */
    abstract boolean isNeedTraceClass(File file)

    void transformDirectory(DirectoryInput directoryInput, File dest) {

        String[] extensions = ["class"]
        //递归地去获取该文件夹下面所有的文件
        Collection<File> fileList = FileUtils.listFiles(directoryInput.file, extensions, true)
        def outputFilePath = dest.absolutePath
        def inputFilePath = directoryInput.file.absolutePath

        println("outputFilePath = $outputFilePath     inputFilePath = $inputFilePath")
        //outputFilePath = E:\Github\GradleStudy\app\build\intermediates\transforms\MethodTimeTransform\free\debug\37
        //inputFilePath  = E:\Github\GradleStudy\app\build\intermediates\javac\freeDebug\classes


        fileList.each { inputFile ->
            //替换前  GradleStudy\app\build\intermediates\javac\freeDebug\classes\com\xfhy\gradledemo\MainActivity$1.class
            //替换后  GradleStudy\app\build\intermediates\transforms\MethodTimeTransform\free\debug\37\com\xfhy\gradledemo\MainActivity$1.class
            println("替换前  file.absolutePath = ${inputFile.absolutePath}")
            def outputFullPath = inputFile.absolutePath.replace(inputFilePath, outputFilePath)
            println("替换后  file.absolutePath = ${outputFullPath}")
            def outputFile = new File(outputFullPath)
            //创建文件
            FileUtils.touch(outputFile)
            //单个单个地复制文件
//            FileUtils.copyFile(file, outputFile)
            transformSingleFile(inputFile, outputFile)
        }

        //如果不处理,则直接复制文件夹给下一个Transform的输入目录就行
        //FileUtils.copyDirectory(directoryInput.file, dest)
    }

    /**
     * 加个打印日志,表示执行到当前Transform了,有标志性,很容易看到
     */
    static void printCopyRight() {
        println()
        println("******************************************************************************")
        println("******                                                                  ******")
        println("******                欢迎使用 Transform 插件                 ******")
        println("******                                                                  ******")
        println("******************************************************************************")
        println()
    }
}