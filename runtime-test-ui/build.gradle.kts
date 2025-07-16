fun properties(key: String) = providers.gradleProperty(key)

plugins {
    // org.jetbrains.intellij version 1.14.1
//    id("org.jetbrains.intellij") version "1.14.1"
    // org.jetbrains.intellij version 1.0
    id("org.jetbrains.intellij") version "1.0"
}

group = "com.zj"
version = "1.1.201"

intellij {
    // org.jetbrains.intellij version 1.14.1
//    version.set(properties("intellij.version"))
//    type.set(properties("intellij.type"))
//
//    plugins.set(listOf("com.intellij.java", "com.intellij.modules.json"))
    // org.jetbrains.intellij version 1.0
    version.set("2020.1.2")
    type.set("IC")

    plugins.set(listOf("com.intellij.java"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        // org.jetbrains.intellij version 1.14.1
//        sinceBuild.set(properties("since.build"))
//        untilBuild.set(properties("until.build"))
        // org.jetbrains.intellij version 1.0
        sinceBuild.set("201")
        untilBuild.set("221.*")
        changeNotes.set(
            """
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