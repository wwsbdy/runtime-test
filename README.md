# runtime-test

Execute Java methods at runtime (including both instance and static methods).<br>
Supports retrieving beans from the Spring container. If no bean is found, the object will be created via its constructor.<br>
Quick Start:<br>
<ul>
    <li>Start the project</li>
    <li>Right-click the target method and select <b>Runtime Test</b></li>
    <li>Fill in the parameters and execute the method</li>
    <li>Code can be written at <b>pre-processing</b> to modify parameter; print log</li>
</ul>

Notes:<br>
<ul>
    <li>Ensure the method belongs to the currently running process</li>
    <li>If the object is created via constructor, the one with the fewest parameters will be chosen, and all arguments will be passed as <code>null</code></li>
    <li>Pre-processing is only available in <b>Debug mode</b></li>
    <li>Pre-processing is based on conditional breakpoints; if the code changed without restarting the project, the breakpoint condition may become invalid</li>
    <li>Do not run <b>Runtime Test</b> before the project has fully started</li>
    <li>Supports JDK 8 and above (theoretically)</li>
</ul>

执行 Java 项目的方法(包括类中的所有方法和静态方法)，获取到Spring容器里的Bean，没有会通过构造器创建<br>
快速开始：<br>
<ul>
    <li>启动项目</li>
    <li>选择要执行的方法，右键选择 Runtime Test </li>
    <li>填充参数，执行方法</li>
    <li>可在<b>前置处理</b>处编写代码用来修改传参；打印日志</li>
</ul>
备注：<br>
<ul>
    <li>确认方法在指定的进程下</li>
    <li>通过构造器创建对象时，会选择传参最少的，参数会传入<code>null</code></li>
    <li>前置处理仅<b>Debug模式</b>有效</li>
    <li>前置处理基于Debug断点的Condition，代码改变后未重新启动项目可能会失效</li>
    <li>项目启动完成之前请勿运行<b>Runtime Test</b></li>
    <li>理论上支持jdk8以上</li>
</ul>

## 配置环境：
- jdk： 17
- gradle：8.1
- org.jetbrains.intellij：1.14.1
- idea： 2024.3

## 部分代码改编自

- https://github.com/lgp547/any-door
- https://github.com/organics2016/pojo2json

## 版本:

### 0.0.1

- 支持Spring项目Bean对象方法调用
- 支持静态方法
- 支持spring-boot-devtools热部署场景
- 点击ok后跳转指定Run/Debug进程窗口
- 支持前置表达式

### 1.1.*

- 兼容其他版本
- 防止项目卡死（断点暂停方法后再运行RuntimeTest，项目会卡死）