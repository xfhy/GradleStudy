package com.xfhy.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

//插件需要借助自定义Task来实现相应的功能
class ReleaseInfoTask extends DefaultTask {
    ReleaseInfoTask() {
        //1. 在构造器中配置了该Task对应的Task group,即Task组,并为其添加上了对应的描述信息
        group = 'version_manager'
        description = 'release info update'
    }

    @TaskAction
    void doAction() {
        //在gradle执行阶段执行
        //!!! 在这里实现自己想要做的东西
        println("卧槽-------")
    }

}