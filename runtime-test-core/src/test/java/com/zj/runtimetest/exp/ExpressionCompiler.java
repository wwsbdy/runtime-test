package com.zj.runtimetest.exp;

import javax.tools.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class ExpressionCompiler {
    public static ExprExecutor.ExpressionExecutor compile(String expr,
                                                          Map<String, String> varTypes, List<String> imports) throws Exception {

        String className = "ExprDynamic_" + Math.abs(expr.hashCode());
        String fullName = "agent." + className;

        StringBuilder sb = new StringBuilder();
        sb.append("package agent;\n");
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("public class ").append(className)
                .append(" implements com.zj.runtimetest.exp.ExprExecutor.ExpressionExecutor {\n");
        sb.append("  public void eval(Object[] args) {\n");

        int i = 0;
        for (Map.Entry<String, String> entry : varTypes.entrySet()) {
            sb.append("    ").append(entry.getValue()).append(" ").append(entry.getKey())
                    .append(" = (").append(entry.getValue()).append(") args[").append(i).append("];\n");
            i++;
        }
        sb.append("    try { ").append(expr).append("; } catch (Throwable t) { t.printStackTrace(); }\n");
        sb.append("  }\n}\n");

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
        return (ExprExecutor.ExpressionExecutor) clazz.getDeclaredConstructor().newInstance();
    }
}