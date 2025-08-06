package com.zj.runtimetest.ui.script;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.zj.runtimetest.constant.Constant;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.utils.NoticeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


/**
 * @author jie.zhou
 */
public class ScriptToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // TODO 先查询缓存
        addContent(project, toolWindow);
        setupAddTabAction(project, toolWindow);
    }

    public void addContent(Project project, ToolWindow toolWindow) {
        if (toolWindow.getContentManager().getContentCount() >= Constant.TAB_MAX) {
            NoticeUtil.notice(project, PluginBundle.get("tool-window.max"));
            return;
        }
        addContent(project, toolWindow, new ScriptEditorPanel(project));
    }

    public void addContent(Project project, ToolWindow toolWindow, ScriptEditorPanel scriptEditorPanel) {
        if (toolWindow.getContentManager().getContentCount() >= Constant.TAB_MAX) {
            NoticeUtil.notice(project, PluginBundle.get("tool-window.max"));
            return;
        }
        Content content = ContentFactory.getInstance()
                .createContent(scriptEditorPanel.getMainPanel(), PluginBundle.get("tool-window.title") + toolWindow.getContentManager().getContentCount(), false);
        content.setCloseable(true);
        Optional.ofNullable(content.getDisposer())
                .ifPresent(disposable -> Disposer.register(toolWindow.getDisposable(), disposable));
        toolWindow.getContentManager().addContent(content);
        toolWindow.getContentManager().setSelectedContent(content);
        Disposable disposable = new Disposable() {
            @Override
            public void dispose() {

            }
        };
        // TODO 关闭和closeAll监听
        content.setDisposer(disposable);
        Disposer.register(toolWindow.getDisposable(), disposable);
        Disposer.register(disposable, scriptEditorPanel);
    }


    private void setupAddTabAction(Project project, ToolWindow toolWindow) {
        // 创建"添加Tab"的动作
        AnAction addTabAction = new ToggleAction(PluginBundle.get("tool-window.add-tab"), "", AllIcons.General.Add) {
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                return false;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean b) {
                addContent(project, toolWindow);
            }
        };
        if (toolWindow instanceof ToolWindowEx) {
            ((ToolWindowEx) toolWindow).setTabActions(addTabAction);
        }
    }
}