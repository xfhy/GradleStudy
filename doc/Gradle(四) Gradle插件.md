
## 1. 前言

依赖`apply plugin: 'com.android.application'`就是依赖了安卓的应用程序插件.然后这个插件里面有android扩展,在[官方文档](http://google.github.io/android-gradle-dsl/current/com.android.build.gradle.AppExtension.html)里面有详细描述.但是,有时候不得不自己写一个插件,方便与业务开展.比如我觉得美团的热修复,在每个方法前面插逻辑的话,肯定得插桩,插桩就得自己写插件.方便快捷.Gradle+ASM可以插桩.有兴趣的可以去了解.

demo地址: https://github.com/xfhy/GradleStudy

## 2. 简单插件

新建一个简单的项目,然后创建一个buildSrc这个名字的module,这个module的名称必须为buildSrc.因为我们创建的这个module是AS专门用来写插件的,会自动参与编译.创建好之后删除Android那一堆东西,什么java代码,res,清单文件等.只剩下build.gradle和.gitignore

![QQleEj.png](https://s2.ax1x.com/2019/12/03/QQleEj.png)

把build.gradle文件内容改成
```gradle
repositories {
    google()
    jcenter()
}
apply {
    plugin 'groovy'
    plugin 'java-gradle-plugin'
}
dependencies {
    implementation gradleApi()
    implementation localGroovy()
    implementation "commons-io:commons-io:2.6"
}
```

然后在main下面创建文件夹groovy,sync一下.没啥问题的话,应该能编译过.然后在groovy文件夹下面创建包名`com.xfhy.plugin`,然后创建一个插件,名字叫CustomPlugin.groovy

```groovy
package com.xfhy.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class CustomPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task('showCustomPlugin') {
            doLast {
                println('hello world plugin!')
            }
        }
    }
}
```

我在这里声明了一个CustomPlugin插件,然后里面创建了一个task名字叫showCustomPlugin,showCustomPlugin插件就只是简单输出一句话. 然后在app的build.gradle里面引入这个插件

```gradle
import com.xfhy.plugin.CustomPlugin
apply plugin: CustomPlugin
```

然后就可以在命令行输入`gradlew showCustomPlugin`执行这个插件.然后就可以在命令行看到输出了.


## 4. 实操

我们来实现一个插件,打包的时候将`pic/test.png`复制到apk文件中的assets中.实际上是在打包之前将test.png复制到相应的文件夹,等待打包合成.首先是新建HookAssetsPlugin.groovy

```groovy
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
```

嵌套特别多,写起来不是很舒服,而且不能智能提示,而且api还不好查....吐血..详细的注释在代码中已给出,这里就不多做介绍了.使用的时候在app下的build.gradle中添加如下代码即可:

```
import com.xfhy.plugin.HookAssetsPlugin
apply plugin: HookAssetsPlugin
```

然后执行`gradlew assembleFreeRelease`,完事儿之后看一下打出来的apk包,可以看到是apk文件里面的assets里面确实多了test.png文件.看起来没什么鸟用,但是却让我们感受到了插件到底可以干些什么事儿.

## 5. 调试Gradle

首先是我们在执行指令的时候加一个参数`gradlew assembleFreeRelease -Dorg.gradle.debug=true  --no-daemon`,这样.
然后在AS->Run->EditConfigurations,然后点击左上角的`+`号,添加一个Remote,然后直接点击apply即可.

![](https://s2.ax1x.com/2019/12/18/Q78Py9.png)

然后点击debug按钮即可进入调试.

![](https://s2.ax1x.com/2019/12/18/Q78dyj.png)


参考

- [写给 Android 开发者的 Gradle 系列（三）撰写 plugin](https://juejin.im/post/5b02113a5188254289190671#heading-5)
- [又掌握了一项新技能 - 断点调试 Gradle 插件](https://fucknmb.com/2017/07/05/%E5%8F%88%E6%8E%8C%E6%8F%A1%E4%BA%86%E4%B8%80%E9%A1%B9%E6%96%B0%E6%8A%80%E8%83%BD-%E6%96%AD%E7%82%B9%E8%B0%83%E8%AF%95Gradle%E6%8F%92%E4%BB%B6/)