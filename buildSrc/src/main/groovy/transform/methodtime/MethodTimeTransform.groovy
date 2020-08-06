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
import org.gradle.api.Project
import org.apache.commons.io.FileUtils

/**
 * 用于统计方法耗时的Transform
 */
class MethodTimeTransform extends Transform {

    Project mProject

    //可以让外界把Project传进来
    MethodTimeTransform(Project project) {
        mProject = project
    }

    @Override
    String getName() {
        return "MethodTime"
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
     * @param transformInvocation
     * @throws TransformException
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        printCopyRight()

        //当前是否是增量编译,由isIncremental方法决定的
        boolean isIncremental = transformInvocation.isIncremental()

        //TransformOutputProvider管理输出路径,如果消费型输入为空,则outputProvider也为空
        TransformOutputProvider outputProvider = transformInvocation.outputProvider

        //transformInvocation.inputs的类型是Collection<TransformInput>,可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        transformInvocation.inputs.each { input -> //这里的input是TransformInput

            input.jarInputs.each { jarInput ->
                //处理jar
                processJarInput(jarInput, outputProvider)
            }

            input.directoryInputs.each { directoryInput ->
                //处理源码文件
                processDirectoryInput(directoryInput, outputProvider)
            }
        }
    }

    void processJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        //将修改过的字节码copy到dest,就可以实现编译期间干预字节码的目的
        println("拷贝文件 $dest -----")
        FileUtils.copyFile(jarInput.file, dest)
    }

    void processDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format
                .DIRECTORY)
        //将修改过的字节码copy到dest,就可以实现编译期间干预字节码的目的
        println("拷贝文件夹 $dest -----")
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    /**
     * 加个打印日志,表示执行到当前Transform了,有标志性,很容易看到
     */
    static void printCopyRight() {
        println()
        println("******************************************************************************")
        println("******                                                                  ******")
        println("******                欢迎使用 MethodTimeTransform 插件                 ******")
        println("******                                                                  ******")
        println("******************************************************************************")
        println()
    }

}
