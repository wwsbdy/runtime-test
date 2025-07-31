package com.zj.runtimetest.exp;

import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

import java.util.Arrays;

/**
 * @author : jie.zhou
 * @date : 2025/7/23
 */
public class StringSourceCompilationUnit implements ICompilationUnit {
    private final String className;
    private final String sourceCode;

    public StringSourceCompilationUnit(String className, String sourceCode) {
//        LogUtil.alwaysLog("className: " + className);
//        LogUtil.alwaysLog("sourceCode: " + sourceCode);
        this.className = className;
        this.sourceCode = sourceCode;
    }

    @Override
    public char[] getContents() {
        return sourceCode.toCharArray();
    }

    @Override
    public char[] getFileName() {
        return (className.replace('.', '/') + ".java").toCharArray();
    }

    @Override
    public char[] getMainTypeName() {
        int dot = className.lastIndexOf('.');
        if (dot > 0) {
            return className.substring(dot + 1).toCharArray();
        }
        return className.toCharArray();
    }

    @Override
    public char[][] getPackageName() {
        String packageName = "";
        int dot = className.lastIndexOf('.');
        if (dot > 0) {
            packageName = className.substring(0, dot);
        }
        return splitToCharArray(packageName);
    }

    private char[][] splitToCharArray(String str) {
        if (str.isEmpty()) {
            return new char[0][];
        }
        return Arrays.stream(str.split("\\.")).map(String::toCharArray).toArray(char[][]::new);
    }

}
