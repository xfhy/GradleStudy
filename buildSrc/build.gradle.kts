plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("greet-plugin") {
            id = "greet"
            implementationClass = "GreetPlugin"
        }
    }
}

repositories {
    maven("http://maven.aliyun.com/nexus/content/groups/public")
    jcenter()
}
