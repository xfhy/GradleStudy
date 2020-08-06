import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

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
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println("执行到我了")
        //在这里对输入输出的class进行处理
        transformInvocation.inputs.each {aa->
            println(aa.directoryInputs)
        }
        //super.transform(transformInvocation)
    }
}
