plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.zj"
version = "0.0.1"

tasks {
    jar {
        manifest {
            attributes["Premain-Class"] = "com.zj.runtimetest.RuntimeTestAttach"
            attributes["Agent-Class"] = "com.zj.runtimetest.RuntimeTestAttach"
//            attributes["Class-Path"] = "byte-buddy-1.16.1.jar byte-buddy-agent-1.16.1.jar"
            attributes["Can-Redefine-Classes"] = "true"
            attributes["Can-Retransform-Classes"] = "true"
        }
    }
}

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
    options.release.set(8)
}

val distDir = project.rootProject.file("dist")
val agentArchive = project.name + ".jar"

tasks.shadowJar {
    destinationDirectory.set(distDir)
    archiveFileName.set(agentArchive)
    relocate("org.objectweb.asm", "com.zj.runtimetest.renamed.asm")
//    dependencies {
//        exclude(dependency("org.springframework:.*"))
//    }
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

