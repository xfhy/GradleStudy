package com.xfhy.plugin

import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Gradle 插件实战
 * 创建plugin,创建task,动态添加或者移除权限
 */
class ManifestDemoPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        //在afterEvaluate,配置完成之后才能拿到那些task完整的有向图
        project.afterEvaluate {
            //1. 找到mergeFreeDebugResources这个task
            def mergeDebugResourcesTask = project.tasks.findByName("mergeFreeDebugResources")
            if (mergeDebugResourcesTask != null) {
                //2. 创建一个task
                def parseDebugTask = project.tasks.create("ParseDebugTask", ParseDebugTask.class)
                //3. 添加一个mergeDebugResourcesTask结束后立马执行的task: parseDebugTask
                mergeDebugResourcesTask.finalizedBy(parseDebugTask)
                mergeDebugResourcesTask.doLast {
                    println("mergeFreeDebugResources 这个task执行完了")
                }
            }
        }
    }
}

class ParseDebugTask extends DefaultTask {

    @TaskAction
    void doAction() {
        println("ParseDebugTask ------ start")
        //1. 找到清单文件这个file
        def file = new File(project.buildDir, "/intermediates/merged_manifests/freeDebug/AndroidManifest.xml")
        //        println(file.getText())
        if (!file.exists()) {
            println("文件不存在")
            return
        }

        //2. 解析xml内容
        def rootNode = new XmlParser().parseText(file.getText())

//        addPermission(rootNode, file)
        removePermission(rootNode, file)
    }

    /**
     * 动态给清单文件添加权限
     * @param rootNode Node
     * @param file 清单文件
     */
    void addPermission(Node rootNode, File file) {
        //3. 添加网络权限  这里得加上xmlns:android
        //<uses-permission android:name="android.permission.INTERNET"/>
        //xmlns:android="http://schemas.android.com/apk/res/android"
        rootNode.appendNode("uses-permission", ["xmlns:android": "http://schemas.android.com/apk/res/android",
                                                "android:name" : "android.permission.INTERNET"])
        rootNode.application[0].appendNode("meta-data", ['android:name': 'appId', 'android:value': 546525])  //还可以加到Application中

        //4. 拿到修改后的xml内容
        def updateXmlContent = XmlUtil.serialize(rootNode)
        println(updateXmlContent)

        //5. 将修改后的xml 写入file中
        file.write(updateXmlContent)
    }

    /**
     * 动态给清单文件移除权限
     * @param rootNode Node
     * @param file 清单文件
     */
    void removePermission(Node rootNode, File file) {
        //方案1  这样会把所有权限都移除了,暂时没找到合适的办法
        //def node = new Node(rootNode, "uses-permission"/*,["android:name" : "android.permission.READ_PHONE_STATE"]*/)
        //rootNode.remove(node)
        //def updateXmlContent = XmlUtil.serialize(rootNode)
        //println(updateXmlContent)

        //方案2 读取到xml内容之后,将制定权限的字符串给替换掉,,妙啊 妙啊
        def manifestContent = file.getText()
        manifestContent = manifestContent.replace("android.permission.READ_PHONE_STATE","")
        println(manifestContent)
        file.write(manifestContent)
    }

}
