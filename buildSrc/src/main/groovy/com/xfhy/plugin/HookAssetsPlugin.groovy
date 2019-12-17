package com.xfhy.plugin

/*import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.PackageApplication*/
import org.gradle.api.Plugin
import org.gradle.api.Project


class HookAssetsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.afterEvaluate {  //获取当前project中的所有task
            project.plugins.withId('com.android.application') {  //确保是Android app project
                project.android.applicationVariants.all { /*ApplicationVariant*/ variant ->   //获取所有变体
                    //println("xfhy === ${variant.name}") //xfhy === freeDebug
                    variant.outputs.each { variantOutput ->    //当前变体输出信息
                        println("xfhy ---- ${variantOutput.name}")
                    }
                    /*if (variant.name.contains('release')) {
                        project.copy {
                            from "${project.projectDir.absolutePath}/pic/test.png"
                            into "${}"
                        }
                    }*/
                }
            }
        }
    }
}