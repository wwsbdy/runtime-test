plugins {
    // idea version 243
//    id("org.jetbrains.intellij") version "1.14.1"
    // idea version 201
    id("org.jetbrains.intellij") version "1.0"
}

group = "com.zj"
version = "1.7.201"

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
            <b>1.7.*</b><br>
            <ul>
                <li>Save entered information even when canceled</li>
                <li>Remove unnecessary cached data</li>
                <li>Fixed the error log sequence issue</li>
                <li>The <b>getBean()</b> method can now be invoked pre-processing to obtain bean objects</li>
                <li>Added a new window for executing scripts on the right side</li>
                <li>Added some methods in pre-processing</li>
            </ul><br>
            <ul>
                <li>取消也保存填写的信息</li>
                <li>去掉不必要的缓存信息</li>
                <li>解决error日志顺序问题</li>
                <li>可在前置处理中调用 <b>getBean()</b> 获取bean对象</li>
                <li>右边侧边栏新增执行脚本窗口</li>
                <li>在前置处理中添加了一些方法</li>
            </ul>
            <b>1.6.*</b><br>
            <ul>
                <li>Fixed cache issues in pre-processing classes</li>
                <li>Added option to print detail logs</li>
            </ul><br>
            <ul>
                <li>调整前置处理类缓存问题</li>
                <li>新增打印详细日志选项</li>
            </ul>
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
            </ul><br>
            <ul>
                <li>移除前置处理对断点的依赖</li>
                <li>支持打印前置处理方法（在前置处理中调用 <b>printPreProcessingMethod()</b> ）</li>
                <li>现在支持 <b>非Debug模式</b> 前置处理</li>
                <li>支持 <b>HttpServletRequest</b> （在前置处理中调用 <b>addHeader()</b> 和 <b>setAttribute()</b> ；参数里支持用json格式填入header）</li>
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