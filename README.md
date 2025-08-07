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

- Maintain compatibility with other versions
- Fix project freeze issue (when resuming execution after breakpoint pause in RuntimeTest)


- 兼容其他版本
- 防止项目卡死（断点暂停方法后再运行RuntimeTest，项目会卡死）

### 1.2.*

- Save entered information even when canceled
- Fix cases where breakpoints were not properly removed
- Skip breakpoint interception for proxy classes


- 取消也保存填写的信息
- 调整一些情况下断点未删除情况
- 跳过代理类的断点拦截

### 1.3.*

- Remove dependency on breakpoints for pre-processing
- Add support for printing pre-processing methods (call <b>printPreProcessingMethod()</b> during pre-processing)
- Now supports pre-processing in <b>non-Debug mode</b>
- Support for HttpServletRequest (in pre-processing, allows calling <b>addHeader()</b> and <b>setAttribute()</b>; supports passing headers in JSON format through parameters)


- 移除前置处理对断点的依赖
- 支持打印前置处理方法（在前置处理中调用 <b>printPreProcessingMethod()</b> ）
- 现在支持 <b>非Debug模式</b> 前置处理
- 支持 <b>HttpServletRequest</b> （在前置处理中调用 <b>addHeader()</b> 和 <b>setAttribute()</b> ；参数里支持用json格式填入header）

### 1.4.*

- Removed dependency on <b>HttpServletRequest</b> in <b>non-Spring</b> projects
- Fixed errors after converting static methods to instance methods
- Support <b>LocalDate</b>, <b>LocalDateTime</b>


- 移除 <b>非Spring</b> 项目对 <b>HttpServletRequest</b> 的依赖
- 调整静态方法改为非静态方法后报错问题
- 支持 <b>LocalDate</b>、<b>LocalDateTime</b>

### 1.5.*

- Adjusted some logic and error issues
- Compatible inner class


- 调整了一些逻辑和报错问题
- 兼容内部类

### 1.6.*

- Fixed cache issues in pre-processing classes
- Added option to print detail logs


- 调整前置处理类缓存问题
- 新增打印详细日志选项

### 1.7.*

- Save entered information even when canceled
- Remove unnecessary cached data
- Fixed the error log sequence issue
- The <b>getBean()</b> method can now be invoked pre-processing to obtain bean objects
- Added a new window for executing scripts on the right side
- Added some methods in pre-processing


- 取消也保存填写的信息
- 去掉不必要的缓存信息
- 解决error日志顺序问题
- 可在前置处理中调用 <b>getBean()</b> 获取bean对象
- 右边侧边栏新增执行脚本窗口
- 在前置处理中添加了一些方法

### 1.8.*

- Script window supports multiple tabs


- 脚本窗口支持多tab