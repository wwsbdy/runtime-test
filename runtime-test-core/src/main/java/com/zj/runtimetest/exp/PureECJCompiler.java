package com.zj.runtimetest.exp;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : jie.zhou
 * @date : 2025/7/23
 */
public class PureECJCompiler {

    public static Class<?> buildClass(String fullName, String sourceCode) {
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
        compiler.compile(new ICompilationUnit[]{new StringSourceCompilationUnit(fullName, sourceCode)});
//        compiler.compile(new ICompilationUnit[]{new CompilationUnit(sourceCode.toString().toCharArray(), fullName.replace('.', '/') + ".java", "UTF-8")});

        byte[] classBytes = compiledClasses.values().iterator().next();
        // 加载类
        // jdk17 不能通过反射调用defineClass
//        ClassLoader appCl = AgentContextHolder.DEFAULT_CLASS_LOADER;
//        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
//        defineClass.setAccessible(true);
//        Class<?> clazz = (Class<?>) defineClass.invoke(appCl, fullName, classBytes, 0, classBytes.length);
        try (RuntimeTestClassLoader runtimeTestClassLoader = RuntimeTestClassLoader.defaultClassLoader()) {
            return runtimeTestClassLoader.publicDefineClass(fullName, classBytes, 0, classBytes.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
