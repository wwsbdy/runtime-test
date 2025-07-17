plugins {
    // idea version 243
//    id("org.jetbrains.intellij") version "1.14.1"
    // idea version 201
    id("org.jetbrains.intellij") version "1.0"
}

group = "com.zj"
version = "1.2.201"

intellij {
    // idea version 243
//    version.set(properties("intellij.version"))
//    type.set(properties("intellij.type"))
//
//    plugins.set(listOf("com.intellij.java", "com.intellij.modules.json"))
    // idea version 201
//    version.set("2020.1.2")
//    version.set("2022.1.4")
//    version.set("2020.2.4")
    version.set("2021.1.1")
    type.set("IC")

    plugins.set(listOf("com.intellij.java"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        // idea version 243
//        sinceBuild.set(properties("since.build"))
//        untilBuild.set(properties("until.build"))
        // idea version 201
        sinceBuild.set("201")
        untilBuild.set("221.*")
        changeNotes.set(
            """
            <b>1.2.*</b><br>
            <ul>
                <li>取消也保存填写的信息</li>
            </ul>
            <b>1.1.*</b><br>
            <ul>
                <li>兼容其他版本</li>
                <li>防止项目卡死</li>
            </ul>
            """
        )
    }

}

dependencies {
    implementation(project(":runtime-test-core"))
}

tasks.assemble {
    dependsOn(tasks.buildPlugin)
}

tasks.buildPlugin {
    dependsOn(":runtime-test-core:jar")
    destinationDirectory.set(project.rootProject.file("dist"))
}

tasks.runIde {
    dependsOn(":runtime-test-core:jar")
}

tasks.clean {
    delete(tasks.buildPlugin.get().archiveFile)
    delete(project.rootProject.file("dist"))
}

tasks.withType<Test>().configureEach {
    enabled = false
}