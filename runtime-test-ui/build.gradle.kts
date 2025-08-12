import java.nio.file.Files

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.intellij") version "1.0"
}

val versionNumber = "1.9"

group = "com.zj"
version = "$versionNumber.201"

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

fun parseChangeNotesFromReadme(): String {
    val readmePath = rootProject.file("README.md").toPath()
    val lines = Files.readAllLines(readmePath)

    data class VersionNotes(val version: String, val blocks: List<List<String>>)

    val versions = mutableListOf<VersionNotes>()

    var currentVersion: String? = null
    var currentBlock = mutableListOf<String>()
    var allBlocks = mutableListOf<List<String>>()

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
        when {
            trimmed.startsWith("### ") -> {
                flushBlock()
                flushVersion()
                val version = trimmed.removePrefix("### ").trim()
                currentVersion = if (version != "0.0.1") version else null
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