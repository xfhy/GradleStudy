package com.xfhy.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class HookAssetsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate { it ->
            //1. 在afterEvaluate闭包中才能获取当前project中的所有task
            project.plugins.withId('com.android.application') {
                //2.确保当前project是Android app project,而不是Android library project, 为给定id的插件执行或注册操作
                project.android.applicationVariants.all { variant ->
                    //3. 拿到所有变体信息
                    //[ApkVariantOutputImpl_Decorated{apkData=Main{type=MAIN, fullName=freeDebug, filters=[], versionCode=2, versionName=2.0.0}}]
                    variant.outputs.each { variantOutput ->
                        // 4. outputs 字段可知道该字段代表着当前变体的输出信息（DomainObjectCollection 类型）
//                        println("${variantOutput.name}")  //free-debug  free-release
                        if (variantOutput.name.contains('release')) {
                            //5. 拿到release
                            project.tasks.findByName("package${variant.name.capitalize()}").doFirst { task ->
                                //6. 得到packageApplication
                                project.copy {
                                    //7. 复制
                                    from "${project.projectDir.absolutePath}/pic/test.png"
                                    into "${task.assets.provider.value.value.getAbsolutePath()}"
                                }

                            }

                        }
                    }
                }
            }
        }
    }
}