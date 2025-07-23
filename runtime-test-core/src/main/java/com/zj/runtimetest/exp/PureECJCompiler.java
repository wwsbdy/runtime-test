package com.zj.runtimetest.exp;

import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.MethodParamTypeInfo;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : jie.zhou
 * @date : 2025/7/23
 */
public class PureECJCompiler {

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
        String packageName = "agent";
        String fullName = packageName + "." + className;

        // 构建 Java 源码
        String source = buildClassStr(parameterTypes, imports, className, expr).toString();
        // 编译配置
        Map<String, String> options = new HashMap<>();
        // 使用你的 JDK 版本
        options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
        options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
        options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
        options.put(CompilerOptions.OPTION_Encoding, "UTF-8");

        Map<String, byte[]> compiledClasses = new HashMap<>();
        Compiler compiler = new Compiler(
                new NameEnv(),
                DefaultErrorHandlingPolicies.ignoreAllProblems(),
                new CompilerOptions(options),
                result -> {
                    if (result.hasErrors()) {
                        for (IProblem problem : result.getProblems()) {
                            if (problem.isError()) {
                                System.err.println(problem);
                            }
                        }
                        throw new RuntimeException("Compilation failed with errors");
                    }

                    for (ClassFile classFile : result.getClassFiles()) {
                        StringBuilder name = new StringBuilder(new String(classFile.getCompoundName()[0]));
                        for (int i = 1; i < classFile.getCompoundName().length; i++) {
                            name.append(".").append(new String(classFile.getCompoundName()[i]));
                        }
                        compiledClasses.put(name.toString(), classFile.getBytes());
                    }
                },
                new DefaultProblemFactory()
        );
        compiler.compile(new ICompilationUnit[]{new StringSourceCompilationUnit(fullName, source)});
//        compiler.compile(new ICompilationUnit[]{new CompilationUnit(source.toString().toCharArray(), fullName.replace('.', '/') + ".java", "UTF-8")});

        byte[] classBytes = compiledClasses.values().iterator().next();
        // 加载类
        // jdk17 不能通过反射调用defineClass
//        ClassLoader appCl = AgentContextHolder.DEFAULT_CLASS_LOADER;
//        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
//        defineClass.setAccessible(true);
//        Class<?> clazz = (Class<?>) defineClass.invoke(appCl, fullName, classBytes, 0, classBytes.length);
        Class<?> clazz;
        try (RuntimeTestClassLoader runtimeTestClassLoader = RuntimeTestClassLoader.defaultClassLoader()) {
            clazz = runtimeTestClassLoader.publicDefineClass(fullName, classBytes, 0, classBytes.length);
        } catch (Exception e) {
            return ExpressionExecutorFactory.ERROR;
        }
        RuntimeTestExprExecutor.ExpressionExecutor expressionExecutor = (RuntimeTestExprExecutor.ExpressionExecutor) clazz.getDeclaredConstructor().newInstance();
        expressionExecutor.setClassStr(source);
        return expressionExecutor;
    }


    private static StringBuilder buildClassStr(List<MethodParamTypeInfo> parameterTypes, List<String> imports, String className, String expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("package agent;\n");
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        sb.append("import com.zj.runtimetest.exp.RuntimeTestExprExecutor.ExpressionExecutor;\n\n");
        sb.append("public class ").append(className)
                .append(" extends ExpressionExecutor {\n")
                .append("    public Object[] eval(Object[] args) {\n");

        for (int i = 0; i < parameterTypes.size(); i++) {
            MethodParamTypeInfo methodParamTypeInfo = parameterTypes.get(i);
            String typeName = methodParamTypeInfo.getType().getTypeName();
            String paramName = methodParamTypeInfo.getParamName();
            sb.append("        ").append(typeName).append(" ").append(paramName)
                    .append(" = (").append(typeName).append(") args[").append(i).append("];\n");
        }
        sb.append("        try {\n").append(expr)
                .append(";\n        } catch (Throwable t) { throw new RuntimeException(t); }\n")
                .append("        System.out.println(\"[Agent] pre-processing execution succeeded\");\n")
                .append("        return new Object[]{");
        for (int i = 0; i < parameterTypes.size(); i++) {
            sb.append(parameterTypes.get(i).getParamName());
            if (i < parameterTypes.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("};\n    }\n");
        sb.append("}\n");
        return sb;
    }

}
