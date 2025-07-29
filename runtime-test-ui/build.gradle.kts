fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("org.jetbrains.intellij") version "1.14.1"
}

group = "com.zj"
version = "1.5.222"

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
            <b>1.5.*</b><br>
            <ul>
                <li>Adjusted some logic and error issues</li>
                <li>Compatible inner class</li>
            </ul><br>
            <ul>
                <li>调整了一些逻辑和报错问题</li>
                <li>兼容内部类</li>
            </ul>
            <b>1.4.*</b><br>
            <ul>
                <li>Removed dependency on <b>HttpServletRequest</b> in <b>non-Spring</b> projects</li>
                <li>Fixed errors after converting static methods to instance methods</li>
                <li>Support <b>LocalDate</b>, <b>LocalDateTime</b></li>
            </ul><br>
            <ul>
                <li>移除 <b>非Spring</b> 项目对 <b>HttpServletRequest</b> 的依赖</li>
                <li>调整静态方法改为非静态方法后报错问题</li>
                <li>支持 <b>LocalDate</b>、<b>LocalDateTime</b></li>
            </ul>
            <b>1.3.*</b><br>
            <ul>
                <li>Remove dependency on breakpoints for pre-processing</li>
                <li>Add support for printing pre-processing methods (call <b>printPreProcessingMethod()</b> during pre-processing)</li>
                <li>Now supports pre-processing in <b>non-Debug mode</b></li>
                <li>Support for HttpServletRequest (in pre-processing, allows calling <b>addHeader()</b> and <b>setAttribute()</b>; supports passing headers in JSON format through parameters)</li>
                <li>Support <b>LocalDate</b>, <b>LocalDateTime</b></li>
            </ul><br>
            <ul>
                <li>移除前置处理对断点对依赖</li>
                <li>支持打印前置处理方法（在前置处理中调用 <b>printPreProcessingMethod()</b> ）</li>
                <li>现在支持 <b>非Debug模式</b> 前置处理</li>
                <li>支持 <b>HttpServletRequest</b> （在前置处理中调用 <b>addHeader()</b> 和 <b>setAttribute()</b> ；参数里支持用json格式填入header）</li>
                <li>支持 <b>LocalDate</b>、<b>LocalDateTime</b></li>
            </ul>
            <b>1.2.*</b><br>
            <ul>
                <li>Save entered information even when canceled</li>
                <li>Fix cases where breakpoints were not properly removed</li>
                <li>Skip breakpoint interception for proxy classes</li>
            </ul><br>
            <ul>
                <li>取消也保存填写的信息</li>
                <li>调整一些情况下断点未删除情况</li>
                <li>跳过代理类的断点拦截</li>
            </ul>
            <b>1.1.*</b><br>
            <ul>
                <li>Maintain compatibility with other versions</li>
                <li>Fix project freeze issue (when resuming execution after breakpoint pause in RuntimeTest)</li>
            </ul><br>
            <ul>
                <li>兼容其他版本</li>
                <li>防止项目卡死</li>
            </ul>
            """
        )
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