package transform.methodtime

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class MethodTimeTransformPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        /*project.plugins.each { plugin ->
            println("$plugin ------")
            if (plugin instanceof BasePlugin) {
                println("$plugin ------   注册成功")
                plugin.registerTransform(new MethodTimeTransform(project))
            }
        }*/

        //project.android.registerTransform(new MethodTimeTransform(project))
    }
}
