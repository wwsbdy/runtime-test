package com.zj.runtimetest.exp;

import com.zj.runtimetest.AgentContextHolder;
import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.MethodParamTypeInfo;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * 表达式编译器
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class RuntimeTestExpCompiler {
    public static RuntimeTestExprExecutor.ExpressionExecutor compile(ExpressionVo expVo, List<MethodParamTypeInfo> parameterTypes, String projectBasePath) throws ClassNotFoundException, NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String expr = expVo.getMyExpression();
        if (Objects.isNull(expr) || expr.isEmpty()) {
            return null;
        }
        List<String> imports = Optional.ofNullable(expVo.getMyCustomInfo())
                .map(ip -> Stream.of(ip.split(";")).collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .orElse(Collections.emptyList());
        Map<String, String> varTypes = parameterTypes.stream()
                .collect(Collectors.toMap(MethodParamTypeInfo::getParamName, type -> type.getType().getTypeName(), (a, b) -> a));
        String className = "ExprDynamic_" + Math.abs(expr.hashCode());
        String fullName = "agent." + className;

        // 构建 Java 源码
        StringBuilder sb = buildClassStr(parameterTypes, imports, className, expr);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new JavaSourceFromString(fullName, sb.toString());
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
//        String outputDir = System.getProperty("java.io.tmpdir") + "/expr_classes";
        String outputDir = projectBasePath + File.separator + ".idea" + File.separator + "runtime-test";
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(new File(outputDir)));
        boolean success = compiler.getTask(null, fileManager, diagnostics, null, null, Collections.singleton(file)).call();
        fileManager.close();

        if (!success) {
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.println(d);
            }
            throw new IllegalStateException("Compilation failed");
        }
        // 不同的类加载器会报java.lang.ClassCastException
        URLClassLoader cl = new URLClassLoader(new URL[]{new File(outputDir).toURI().toURL()});
        Class<?> clazz = cl.loadClass(fullName);
        return (RuntimeTestExprExecutor.ExpressionExecutor) clazz.getDeclaredConstructor().newInstance();
    }

    public static RuntimeTestExprExecutor.ExpressionExecutor compileInMemory(ExpressionVo expVo, List<MethodParamTypeInfo> parameterTypes) throws Exception {

        String expr = expVo.getMyExpression();
        if (expr == null || expr.isEmpty()) {
            return ExpressionExecutorFactory.EMPTY;
        }

        List<String> imports = Optional.ofNullable(expVo.getMyCustomInfo())
                .map(ip -> Stream.of(ip.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());


        String className = "ExprDynamic_" + Math.abs(expr.hashCode());
        String fullName = "agent." + className;

        // 构建 Java 源码
        StringBuilder sb = buildClassStr(parameterTypes, imports, className, expr);
        // 内存中编译
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject source = new JavaSourceFromString(fullName, sb.toString());

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        InMemoryClassFileManager fileManager = new InMemoryClassFileManager(
                compiler.getStandardFileManager(diagnostics, null, null)
        );

        boolean success = compiler.getTask(null, fileManager, diagnostics, null, null, Collections.singleton(source)).call();
        if (!success) {
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.println(d);
            }
            throw new IllegalStateException("In-memory compilation failed");
        }

        // 获取 class 字节码
        byte[] classBytes = fileManager.getClassBytes(fullName);
        ClassLoader appCl = AgentContextHolder.DEFAULT_CLASS_LOADER;

        // 加载类
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        Class<?> clazz = (Class<?>) defineClass.invoke(appCl, fullName, classBytes, 0, classBytes.length);

        return (RuntimeTestExprExecutor.ExpressionExecutor) clazz.getDeclaredConstructor().newInstance();
    }

    private static StringBuilder buildClassStr(List<MethodParamTypeInfo> parameterTypes, List<String> imports, String className, String expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("package agent;\n");
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }

        sb.append("public class ").append(className)
                .append(" implements com.zj.runtimetest.exp.RuntimeTestExprExecutor.ExpressionExecutor {\n")
                .append("  public Object[] eval(Object[] args) {\n");

        for (int i = 0; i < parameterTypes.size(); i++) {
            MethodParamTypeInfo methodParamTypeInfo = parameterTypes.get(i);
            String typeName = methodParamTypeInfo.getType().getTypeName();
            String paramName = methodParamTypeInfo.getParamName();
            sb.append("    ").append(typeName).append(" ").append(paramName)
                    .append(" = (").append(typeName).append(") args[").append(i).append("];\n");
        }
        sb.append("    try { ").append(expr).append("; } catch (Throwable t) { throw new RuntimeException(t); }\n")
                .append("    System.out.println(\"[Agent] pre-processing execution succeeded\");")
                .append("    return new Object[]{");
        for (int i = 0; i < parameterTypes.size(); i++) {
            sb.append(parameterTypes.get(i).getParamName());
            if (i < parameterTypes.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("};\n  }\n}\n");
        return sb;
    }
}