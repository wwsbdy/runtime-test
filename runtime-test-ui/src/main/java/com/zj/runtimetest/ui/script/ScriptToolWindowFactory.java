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
import com.intellij.ui.content.*;
import com.zj.runtimetest.cache.RuntimeTestState;
import com.zj.runtimetest.constant.Constant;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.utils.NoticeUtil;
import com.zj.runtimetest.utils.PluginCacheUtil;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.ItemVo;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * @author jie.zhou
 */
public class ScriptToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 先查询缓存
        RuntimeTestState runtimeTestState = RuntimeTestState.getInstance(project);
        List<ItemVo<CacheVo>> itemVoList = PluginCacheUtil.getAllExistContentCacheKey(runtimeTestState);
        if (CollectionUtils.isNotEmpty(itemVoList)) {
            for (ItemVo<CacheVo> itemVo : itemVoList) {
                addContent(project, toolWindow, itemVo.getIndex(), new ScriptEditorPanel(project, itemVo.getValue()));
            }
        } else {
            addContent(project, toolWindow);
        }
        setupAddTabAction(project, toolWindow);
    }

    public static void addContent(Project project, ToolWindow toolWindow) {
        addContent(project, toolWindow, null);
    }

    public static void addContent(Project project, ToolWindow toolWindow, ExpressionVo expressionVo) {
        if (toolWindow.getContentManager().getContentCount() >= Constant.TAB_MAX) {
            NoticeUtil.notice(project, PluginBundle.get("tool-window.max"));
            return;
        }
        RuntimeTestState runtimeTestState = RuntimeTestState.getInstance(project);
        ItemVo<String> keyItem = PluginCacheUtil.getOneOfNotExistContentCacheKey(runtimeTestState);
        if (Objects.isNull(keyItem)) {
            NoticeUtil.notice(project, PluginBundle.get("tool-window.create-content-error"));
            return;
        }
        CacheVo cacheVo = new CacheVo();
        cacheVo.setExpVo(expressionVo);
        runtimeTestState.putCache(keyItem.getValue(), cacheVo);
        addContent(project, toolWindow, keyItem.getIndex(), new ScriptEditorPanel(project, cacheVo));
    }

    public static void addContent(Project project, ToolWindow toolWindow, Integer index, ScriptEditorPanel scriptEditorPanel) {
        ContentManager contentManager = toolWindow.getContentManager();
        if (contentManager.getContentCount() >= Constant.TAB_MAX) {
            NoticeUtil.notice(project, PluginBundle.get("tool-window.max"));
            return;
        }
        Content content = ContentFactory.getInstance()
                .createContent(scriptEditorPanel.getMainPanel(), PluginBundle.get("tool-window.title") + index, false);
        content.setCloseable(true);
        Optional.ofNullable(content.getDisposer())
                .ifPresent(disposable -> Disposer.register(toolWindow.getDisposable(), disposable));
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
        Disposable disposable = new Disposable() {
            @Override
            public void dispose() {

            }
        };
        // 添加Content关闭监听
        contentManager.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void contentRemoved(@NotNull ContentManagerEvent event) {
                if (event.getContent() == content) {
                    // 关闭Content时移除缓存
                    RuntimeTestState.getInstance(project).removeCache(Constant.TOOLWINDOW_CONTENT_CACHE_KEY_LIST.get(index));
                }
            }
        });
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