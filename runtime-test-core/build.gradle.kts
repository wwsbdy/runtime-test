plugins {
    // idea version 243
//    id("com.github.johnrengelman.shadow") version "8.1.1"
    // idea version 201
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.zj"
version = "0.0.1"

dependencies {
//    implementation("net.bytebuddy:byte-buddy:1.16.1")
//    implementation("net.bytebuddy:byte-buddy-agent:1.16.1")
//    compileOnly("org.springframework:spring-context:5.3.30")
    implementation("org.ow2.asm:asm:9.7.1")
    implementation("org.ow2.asm:asm-tree:9.7.1")
    implementation("org.ow2.asm:asm-analysis:9.7.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
}

tasks.withType<JavaCompile>().configureEach {
//    options.release.set(8)
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

val distDir = project.rootProject.file("dist")
val agentArchive = project.name + ".jar"

tasks.shadowJar {
    destinationDirectory.set(distDir)
    archiveFileName.set(agentArchive)
    relocate("org.objectweb.asm", "com.zj.runtimetest.renamed.asm")
    manifest {
        attributes(
            mapOf(
                "Premain-Class" to "com.zj.runtimetest.RuntimeTestAttach",
                "Agent-Class" to "com.zj.runtimetest.RuntimeTestAttach",
                "Boot-Class-Path" to agentArchive,
                "Can-Redefine-Classes" to "true",
                "Can-Retransform-Classes" to "true"
            )
        )
    }
}

tasks.jar {
    destinationDirectory.set(distDir)
    archiveFileName.set(agentArchive)
    enabled = false
    dependsOn(tasks.shadowJar)
}

tasks.clean {
    delete(distDir.resolve(agentArchive))
}

tasks.test {
    dependsOn(tasks.jar)
    jvmArgs = listOf(
        "-Djdk.attach.allowAttachSelf=true"
//        "-javaagent:${distDir}${File.separator}${agentArchive}"
    )
}


