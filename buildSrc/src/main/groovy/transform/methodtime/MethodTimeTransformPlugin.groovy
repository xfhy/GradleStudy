package transform.methodtime

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class MethodTimeTransformPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        //注册方式1
        AppExtension appExtension = project.extensions.getByType(AppExtension)
        appExtension.registerTransform(new MethodTimeTransform(project))
        //注册之后会在TransformManager#addTransform中生成一个task.

        //注册方式2
        //project.android.registerTransform(new MethodTimeTransform(project))
    }
}
