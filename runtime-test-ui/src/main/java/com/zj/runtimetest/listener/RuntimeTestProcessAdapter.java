package com.zj.runtimetest.listener;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.zj.runtimetest.utils.BreakpointUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 监听agent报错日志，清空断点
 * @author : jie.zhou
 * @date : 2025/7/14
 */
public class RuntimeTestProcessAdapter extends ProcessAdapter {

    private final Project project;

    public RuntimeTestProcessAdapter(Project project) {
        super();
        this.project = project;
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String text = event.getText();
        if (text.startsWith("[Agent]") && ProcessOutputType.isStderr(outputType)) {
            // 这里是清空断点，小概率会把其他RuntimeTest请求的前置处理删除
            BreakpointUtil.removeBreakpoints(project);
            return;
        }
        super.onTextAvailable(event, outputType);
    }
}
