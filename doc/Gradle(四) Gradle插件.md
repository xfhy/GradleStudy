
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

然后就可以在命令行输入`gradlew showCustomPlugin`执行这个插件.然后就可以在命令行看到输出了.就这样简单的几步,一个简单的插件就写完了,虽然只是简单地打印了`hello world.`.

## 3. 插件 plus

但是,我们肯定不会满足于只会输出hello world的插件,我们来做一个稍微有点用的插件.正如我们平常在android{} 闭包里面,我们写了很多次的

```gradle
compileSdkVersion Config.compileSdkVersion
buildToolsVersion Config.buildToolsVersion
....
```

那我们就来做这样一个插件,读取这些配置信息的数据.首先在包名`com.xfhy.plugin`下面创建`AndroidExtension.groovy`,它是一个bean对象,用来保存信息的.定义如下:

```groovy
package com.xfhy.plugin

class AndroidExtension {
    String compileSdkVersion = ''
    String buildToolsVersion = ''
    String applicationId = ''
    String minSdkVersion = ''
}
```

然后创建一个新的插件`MyAndroidPlugin.groovy`

```groovy
package com.xfhy.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class MyAndroidPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('testExtension', AndroidExtension)
        project.task('AndroidPlugin') {
            doLast {
                println("minSdkVersion = ${extension.minSdkVersion}")
                println("applicationId = ${extension.applicationId}")
                println("compileSdkVersion = ${extension.compileSdkVersion}")
                println("buildToolsVersion = ${extension.buildToolsVersion}")
            }
        }
    }
}

```

通过`project.extensions.create`来获取testExtension闭包中的内容,并通过反射将闭包中的内容转成一个AndroidExtension对象.然后我创建了一个task去读取它.

然后我们在app下面的build.gradle中加入代码:

```gradle
testExtension {
    minSdkVersion '22'
    applicationId 'com.xfhy.gradledemo'
    compileSdkVersion '29'
    buildToolsVersion '29.0.2'
}
```

输出如下:

```
minSdkVersion = 22
applicationId = com.xfhy.gradledemo
compileSdkVersion = 29
buildToolsVersion = 29.0.2
```

不错,已经能搞一个能读取到闭包信息的插件了.
