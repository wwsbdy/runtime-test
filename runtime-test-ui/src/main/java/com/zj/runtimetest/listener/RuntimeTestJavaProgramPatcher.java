package com.zj.runtimetest.listener;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleRunConfiguration;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;

import java.io.File;

/**
 * 启动修补器，将agent注入进程
 *
 * @author 19242
 */
public class RuntimeTestJavaProgramPatcher extends JavaProgramPatcher {
    @Override
    public void patchJavaParameters(Executor executor, RunProfile runProfile, JavaParameters javaParameters) {
        if (runProfile instanceof ModuleRunConfiguration) {
            Sdk jdk = javaParameters.getJdk();
            if (jdk != null && jdk.getSdkType() instanceof JavaSdk) {
                JavaSdkVersion version = JavaSdk.getInstance().getVersion(jdk);
                if (version != null && version.isAtLeast(JavaSdkVersion.JDK_1_9)) {
                    // 添加 --add-opens
                    ParametersList vmOptions = javaParameters.getVMParametersList();
                    vmOptions.add("--add-opens");
                    vmOptions.add("java.base/java.lang=ALL-UNNAMED");
                }
            }
            String coreJarPath = PathManager.getPluginsPath() + File.separator + "runtime-test-ui" + File.separator + "lib" + File.separator + "runtime-test-core.jar";
            javaParameters.getVMParametersList().add("-javaagent:" + coreJarPath);
        }
    }
}
