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
import org.gradle.api.Project
import org.apache.commons.io.FileUtils

import java.util.concurrent.Callable

/**
 * 用于统计方法耗时的Transform
 */
class MethodTimeTransform extends Transform {

    private WaitableExecutor mWaitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
    private Project mProject

    //可以让外界把Project传进来
    MethodTimeTransform(Project project) {
        mProject = project
    }

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
            transformDirectory(directoryInput.file, dest)
        }
    }

    void transformSingleFile(File inputFile, File destFile) {
        println("拷贝单个文件")
        FileUtils.copyFile(inputFile, destFile)
    }

    void transformDirectory(File directoryInputFile, File dest) {
        println("拷贝文件夹 $dest -----")
        FileUtils.copyDirectory(directoryInputFile, dest)
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
