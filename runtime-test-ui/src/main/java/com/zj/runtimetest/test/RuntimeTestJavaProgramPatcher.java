package com.zj.runtimetest.test;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.application.PathManager;

import java.io.File;

/**
 * 启动修补器，将agent注入进程
 * @author 19242
 */
public class RuntimeTestJavaProgramPatcher extends JavaProgramPatcher {
    @Override
    public void patchJavaParameters(Executor executor, RunProfile runProfile, JavaParameters javaParameters) {
        String coreJarPath = PathManager.getPluginsPath() + File.separator + "runtime-test-ui" + File.separator + "lib" + File.separator + "runtime-test-core.jar";
        javaParameters.getVMParametersList().add("-javaagent:" + coreJarPath);
    }
}
