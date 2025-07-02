fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("org.jetbrains.intellij") version "1.14.1"
    kotlin("jvm")
}

group = "com.zj"
version = "0.0.1"

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.3.4")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java", "com.intellij.modules.json"))
}


//repositories {
//  maven("https://maven.aliyun.com/nexus/content/groups/public/")
//  maven("https://maven.aliyun.com/repository/public/")
//  maven("https://maven.aliyun.com/repository/google/")
//  maven("https://maven.aliyun.com/repository/jcenter/")
//  maven("https://maven.aliyun.com/repository/central/")
//  google()
//}
tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }

    register("prepareKotlinBuildScriptModel")
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":runtime-test-core")) {
        isTransitive = false
    }
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
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
}