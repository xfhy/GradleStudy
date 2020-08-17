package transform.base

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BaseTransformPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        //注册方式1
        AppExtension appExtension = project.extensions.getByType(AppExtension)
        appExtension.registerTransform(getCustomTransform(project))
        //注册之后会在TransformManager#addTransform中生成一个task.

        //注册方式2
        //project.android.registerTransform(getCustomTransform())
    }

    /**
     * 需要注册的自定义Transform
     */
    abstract Transform getCustomTransform(Project project)

}