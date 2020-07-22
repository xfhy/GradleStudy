
import org.gradle.api.*

println("hello world")

//tasks用于创建和操作任务
//tasks其实是NamedDomainObjectContainer,查api到这里 https://docs.gradle.org/6.1.1/dsl/org.gradle.api.NamedDomainObjectContainer.html

//通过tasks注册foo这个task
//tasks.create("foo") {
//    println("${name} 配置阶段---")
//
//    doLast {
//        println("${name} 执行阶段---")
//    }
//}

//获取task
//val foo = tasks["foo"]
//tasks.getByName("foo").name
//val foo by tasks.getting

//常用的内建task

//这个是拷贝
//tasks.create<Copy>("myCopy") {
//    from("build.gradle.kts")
//
//    //需要包含哪些文件
//    include {  }
//    //需要排除哪些文件
//    exclude {  }
//
//    into("$buildDir")
//}

//这个是project对象的api,也是复制
copy {
    from("build.gradle.kts")
    into("$buildDir")
}

//压缩
tasks.create<Zip>("myZip") {
    //待压缩的文件目录
    from("$buildDir")
    //输出目录
    destinationDirectory.set(file("$buildDir/dist"))
    //设置输出文件名
    archiveFileName.set("myZip.zip")
}