package com.zj.runtimetest.exp;

import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.MethodParamInfo;
import com.zj.runtimetest.vo.RequestInfo;

import javax.tools.*;
import java.io.File;
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
    public static RuntimeTestExprExecutor.ExpressionExecutor compile(RequestInfo requestInfo) throws Exception {
        ExpressionVo expVo = requestInfo.getExpVo();
        String expr = expVo.getMyExpression();
        if (Objects.isNull(expr) || expr.isEmpty()) {
            return null;
        }
        List<String> imports = Optional.ofNullable(expVo.getMyCustomInfo())
                .map(ip -> Stream.of(ip.split(";")).collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .orElse(Collections.emptyList());
        Map<String, String> varTypes = requestInfo.getParameterTypeList().stream()
                .collect(Collectors.toMap(MethodParamInfo::getParamName, MethodParamInfo::getParamType, (a, b) -> a));
        String className = "ExprDynamic_" + Math.abs(expr.hashCode());
        String fullName = "agent." + className;

        StringBuilder sb = new StringBuilder();
        sb.append("package agent;\n");
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("public class ").append(className)
                .append(" implements com.zj.runtimetest.exp.RuntimeTestExprExecutor.ExpressionExecutor {\n");
        sb.append("  public Object[] eval(Object[] args) {\n");

        int i = 0;
        for (Map.Entry<String, String> entry : varTypes.entrySet()) {
            sb.append("    ").append(entry.getValue()).append(" ").append(entry.getKey())
                    .append(" = (").append(entry.getValue()).append(") args[").append(i).append("];\n");
            i++;
        }
        sb.append("    try { ").append(expr).append("; } catch (Throwable t) { t.printStackTrace(); }\n");
        sb.append("    ").append("return new Object[]{");

        varTypes.keySet().forEach(key -> sb.append(key).append(", "));
        // 如果末尾是", "，去掉
        if (sb.charAt(sb.length() - 2) == ',') {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("};\n");
        sb.append("  }\n}\n");
        Object[] a = {""};
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new JavaSourceFromString(fullName, sb.toString());
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
//        String outputDir = System.getProperty("java.io.tmpdir") + "/expr_classes";
        String outputDir = System.getProperty("user.dir").replace("runtime-test-core", "") + "dist";
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(new File(outputDir)));
        boolean success = compiler.getTask(null, fileManager, diagnostics, null, null, Collections.singleton(file)).call();
        fileManager.close();

        if (!success) {
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                System.err.println(d);
            }
            throw new IllegalStateException("Compilation failed");
        }

        URLClassLoader cl = new URLClassLoader(new URL[]{new File(outputDir).toURI().toURL()});
        Class<?> clazz = cl.loadClass(fullName);
        return (RuntimeTestExprExecutor.ExpressionExecutor) clazz.getDeclaredConstructor().newInstance();
    }
}