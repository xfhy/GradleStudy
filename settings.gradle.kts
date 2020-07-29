//init.gradle 对应Gradle对象
//settings.gradle 对应的是Settings对象,API 文档: https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html#org.gradle.api.initialization.Settings
//build.gradle 对应的是Project对象

rootProject.name = "Gradle学习"

//设置参与构建的模块
include(
        ":app"
)
rootProject.buildFileName = "build.gradle.kts"
/*for (project in rootProject.children) {
    project.apply {
        buildFileName = "build.gradle.kts"
    }
}*/
