[//]: # (version-start)

### 1.1.* - 2.3.*

- Remove dependency on breakpoints for pre-processing
- Now supports pre-processing in <b>non-Debug mode</b>
- Support for HttpServletRequest (in pre-processing, allows calling <b>addHeader()</b> and <b>setAttribute()</b>;
  supports passing headers in JSON format through parameters)
- Add support for printing pre-processing methods (call <b>printPreProcessingMethod()</b> during pre-processing)
- The <b>getBean()</b> method can now be invoked pre-processing to obtain bean objects
- Added some methods in pre-processing
- Right-click method can quickly generate call scripts (<b>Runtime Test Script</b> action)


- 移除前置处理对断点的依赖
- 现在支持 <b>非Debug模式</b> 前置处理
- 支持 <b>HttpServletRequest</b> （在前置处理中调用 <b>addHeader()</b> 和 <b>setAttribute()</b> ；参数里支持用json格式填入header）
- 支持打印前置处理方法（在前置处理中调用 <b>printPreProcessingMethod()</b> ）
- 可在前置处理中调用 <b>getBean()</b> 获取bean对象
- 在前置处理中添加了一些方法
- 右键方法可快速生成调用脚本（<b>Runtime Test Script</b> 功能）

### 2.4.*

- Remove jackson


- 移除jackson

[//]: # (version-end)