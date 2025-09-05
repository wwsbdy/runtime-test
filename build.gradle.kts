plugins {
    id("java")
}

group = "com.zj"
version = "0.0.1"

subprojects {
    apply {
        plugin("java")
    }
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/jcenter/")
        maven("https://maven.aliyun.com/repository/central/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
        testImplementation("junit:junit:4.13.1")
        testCompileOnly("org.projectlombok:lombok:1.18.34")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    }
}
