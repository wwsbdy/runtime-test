import java.nio.file.Files

fun properties(key: String) =
    project.findProperty(key)?.toString() ?: throw IllegalStateException("Property `$key` is undefined")

plugins {
    id("org.jetbrains.intellij") version "1.0"
}

group = "com.zj"
version = "${properties("plugin.version")}.201"

intellij {
    version.set(properties("intellij.version"))
    type.set(properties("intellij.type"))
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(properties("since.build"))
        untilBuild.set(properties("until.build"))
        changeNotes.set(parseChangeNotesFromReadme())
    }

}

dependencies {
    implementation(files("../dist/runtime-test-core.jar"))
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

tasks.compileJava {
    dependsOn(":runtime-test-core:jar")
}

tasks.publishPlugin {
    token.set(properties("publish.token"))
}

fun parseChangeNotesFromReadme(): String {
    val readmePath = rootProject.file("README.md").toPath()
    val lines = Files.readAllLines(readmePath)

    data class VersionNotes(val version: String, val blocks: List<List<String>>)

    val versions = mutableListOf<VersionNotes>()

    var currentVersion: String? = null
    var currentBlock = mutableListOf<String>()
    var allBlocks = mutableListOf<List<String>>()
    var isVersionBlock = false

    fun flushBlock() {
        if (currentBlock.isNotEmpty()) {
            allBlocks.add(currentBlock)
            currentBlock = mutableListOf()
        }
    }

    fun flushVersion() {
        if (currentVersion != null && allBlocks.isNotEmpty()) {
            versions.add(VersionNotes(currentVersion!!, allBlocks.toList()))
        }
        allBlocks = mutableListOf()
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("[//]: # (version-start)")) {
            isVersionBlock = true
        }
        if (!isVersionBlock) {
            continue
        }
        // 检测版本块结束标记
        if (trimmed.startsWith("[//]: # (version-end)")) {
            break
        }
        when {
            trimmed.startsWith("### ") -> {
                flushBlock()
                flushVersion()
                currentVersion = trimmed.removePrefix("### ").trim()
            }

            trimmed.isEmpty() -> {
                flushBlock()
            }

            trimmed.startsWith("- ") && currentVersion != null -> {
                currentBlock.add(trimmed.removePrefix("- ").trim())
            }
        }
    }
    flushBlock()
    flushVersion()

    // 倒序（新版本在最前面）
    versions.reverse()

    val result = StringBuilder()
    for (v in versions) {
        result.append("<h3>${v.version}</h3>\n")
        for (block in v.blocks) {
            result.append("<ul>\n")
            block.forEach { item ->
                result.append("    <li>${item}</li>\n")
            }
            result.append("</ul><br>\n")
        }
    }

    return result.toString()
}