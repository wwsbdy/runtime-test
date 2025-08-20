package com.zj.runtimetest.exp;

import com.zj.runtimetest.AgentContextHolder;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 类加载器
 * @author : jie.zhou
 * @date : 2025/7/23
 */
public class RuntimeTestClassLoader extends URLClassLoader {


    private RuntimeTestClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public Class<?> publicDefineClass(String name, byte[] b, int off, int len) throws ClassFormatError {
        return super.defineClass(name, b, off, len);
    }

    public static class Default {
        static final RuntimeTestClassLoader INSTANCE = new RuntimeTestClassLoader(AgentContextHolder.DEFAULT_CLASS_LOADER);
    }

    public static RuntimeTestClassLoader defaultClassLoader() {
        return Default.INSTANCE;
    }
}
