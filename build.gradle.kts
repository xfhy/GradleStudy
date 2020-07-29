// Top-level build file where you can add configuration options common to all sub-projects/modules.

//apply from: 'life.gradle'

buildscript {
    repositories {
        maven("http://maven.aliyun.com/nexus/content/groups/public")
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.2")
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
    }
}

//这里扩展了ext这个属性
ext {
    set("compileSdkVersion", "29")
    set("buildToolsVersion", "29.0.2")

    //额外属性   使用方式:在build.gradle中 property("test_version") 即可取到
    set("test_version", "1.3.50")
}

allprojects {
    //设置依赖查找仓库,repositories对应的是RepositoryHandler对象  https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.dsl.RepositoryHandler.html
    repositories {
        maven("http://maven.aliyun.com/nexus/content/groups/public")
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
