# runtime-test

Execute any method of the Java project (including all methods in the class and static methods), get bean from the Spring container, without creating it through a constructor.<br>
Getting Started:<br>
<ul>
    <li>run project</li>
    <li>select method and right-click and choose 'Runtime Test'</li>
    <li>fill parameters, execute method</li>
</ul>
ps:<br>
<ul>
    <li>please confirm the method in the selected process</li>
    <li>not a Spring bean, it will choose the constructor with the least number of parameters to create the object, and the parameters will be passed as NULL</li>
</ul>

执行 Java 项目的方法(包括类中的所有方法和静态方法)，获取到Spring容器里的Bean，没有会通过构造器创建<br>
快速开始：<br>
<ul>
    <li>启动项目</li>
    <li>选择要执行的方法，右键选择 Runtime Test </li>
    <li>填充参数，执行方法</li>
</ul>
备注：<br>
<ul>
    <li>确认方法在指定的进程下</li>
    <li>通过构造器创建对象时，会选择传参最少的，参数会传入NULL</li>
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