//package com.zj.runtimetest.ui.script;
//
//import com.intellij.openapi.Disposable;
//import com.intellij.openapi.actionSystem.ActionUpdateThread;
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.diagnostic.Logger;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.util.Disposer;
//import com.intellij.openapi.wm.ToolWindow;
//import com.intellij.ui.content.Content;
//import com.intellij.ui.content.ContentFactory;
//import com.intellij.xdebugger.impl.ui.XDebuggerExpressionEditor;
//import com.zj.runtimetest.language.PluginBundle;
//import com.zj.runtimetest.ui.ExpressionEditorFactory;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Optional;
//
//
//public class ExecutionScriptAction extends AnAction implements Disposable {
//
//    private static final Logger log = Logger.getInstance(ExecutionScriptAction.class);
//
//    @Override
//    public void actionPerformed(@NotNull final AnActionEvent e) {
//        final Project project = e.getProject();
//        try {
//
//        } catch (Exception exception) {
//            log.error("invoke exception", exception);
//        }
//    }
//
//    @Override
//    public @NotNull ActionUpdateThread getActionUpdateThread() {
//        return ActionUpdateThread.BGT;
//    }
//
//    @Override
//    public void dispose() {
//
//    }
//
//    public static void addContent(Project project, ToolWindow toolWindow) {
//        XDebuggerExpressionEditor expressionEditor = ExpressionEditorFactory.createExpressionEditor(project, null, null);
//        Content content = ContentFactory.getInstance()
//                .createContent(expressionEditor.getComponent(), PluginBundle.get("tool-window.title") + toolWindow.getContentManager().getContentCount(), false);
//        content.setCloseable(true);
//        Optional.ofNullable(content.getDisposer())
//                .ifPresent(disposable -> Disposer.register(toolWindow.getDisposable(), disposable));
//        toolWindow.getContentManager().addContent(content);
//        toolWindow.getContentManager().setSelectedContent(content);
//        Optional.ofNullable(content.getDisposer())
//                .ifPresent(disposable -> Disposer.register(toolWindow.getDisposable(), disposable));
//    }
//}
