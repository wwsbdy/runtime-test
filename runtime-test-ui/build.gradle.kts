fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("org.jetbrains.intellij") version "1.14.1"
}

group = "com.zj"
version = "1.3.0"

intellij {
    version.set(properties("intellij.version"))
    type.set(properties("intellij.type"))
    // idea version 243
//    plugins.set(listOf("com.intellij.java", "com.intellij.modules.json"))

    plugins.set(listOf("com.intellij.java"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(properties("since.build"))
        untilBuild.set(properties("until.build"))
        changeNotes.set(
            """
            <b>1.3.*</b><br>
            <ul>
                <li>调整断点定位逻辑</li>
            </ul>
            <b>1.2.*</b><br>
            <ul>
                <li>取消也保存填写的信息</li>
                <li>调整一些情况下断点未删除情况</li>
                <li>跳过代理类的断点拦截</li>
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