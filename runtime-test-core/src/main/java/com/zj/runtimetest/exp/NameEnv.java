package com.zj.runtimetest.exp;

import com.zj.runtimetest.AgentContextHolder;
import com.zj.runtimetest.utils.IOUtil;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

import java.io.InputStream;
import java.util.Objects;

/**
 * ECJ NameEnvironment (mock，所有类由默认 ClassLoader 提供)
 *
 * @author : jie.zhou
 * @date : 2025/7/23
 */
public class NameEnv implements INameEnvironment {
    @Override
    public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
        return findType(CharOperation.toString(compoundTypeName));
    }

    @Override
    public NameEnvironmentAnswer findType(char[] typeName, char[][] pkgName) {
        return findType(CharOperation.toString(pkgName) + "." + new String(typeName));
    }

    private NameEnvironmentAnswer findType(String className) {
        try {
            String res = className.replace('.', '/') + ".class";
            try (InputStream inputStream = AgentContextHolder.DEFAULT_CLASS_LOADER.getResourceAsStream(res)) {
                if (Objects.isNull(inputStream)) {
                    return null;
                }
                byte[] bytes = IOUtil.readAllBytes(inputStream);
                return new NameEnvironmentAnswer(
                        new ClassFileReader(bytes, className.toCharArray()), null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isPackage(char[][] parentPackageName, char[] packageName) {
        return true;
    }

    @Override
    public void cleanup() {
    }
}
