package com.zj.runtimetest.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.zj.runtimetest.utils.BreakpointUtil;
import com.zj.runtimetest.utils.ParamUtil;
import com.zj.runtimetest.utils.PluginCacheUtil;
import com.zj.runtimetest.utils.RunUtil;
import com.zj.runtimetest.vo.CacheVo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;


/**
 * Original work: AnyDoorOpenAction - Copyright (c) 2025 [lgp547]
 * Modifications/adjustments made by [jie.zhou] on [2025/6/30].
 * Key modifications:
 * 1. [调整调用窗口]
 * 2. [调整请求信息]
 * Source: <a href="https://github.com/lgp547/any-door">any-door</a>
 * License: [Apache 2.0]
 * @author jie.zhou
 * @date : 2025/7/2
 */
public class RuntimeTestAction extends AnAction implements Disposable {

    private static final Logger log = Logger.getInstance(RuntimeTestAction.class);

    private final static Key<PsiMethod> USER_DATE_ELEMENT_KEY = new Key<>("user.psi.Element");

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (null == project || editor == null) {
            throw new IllegalArgumentException("idea arg error (project or editor is null)");
        }
        XLineBreakpoint<JavaMethodBreakpointProperties> bp = null;
        try {
            PsiMethod psiMethod = null;
            if (e.getDataContext() instanceof UserDataHolder) {
                psiMethod = ((UserDataHolder) e.getDataContext()).getUserData(USER_DATE_ELEMENT_KEY);
            }
            if (psiMethod == null) {
                PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
                psiMethod = PsiTreeUtil.getParentOfType(getElement(editor, file), PsiMethod.class);
                if (psiMethod == null) {
                    throw new IllegalArgumentException("idea arg error (method is null)");
                }
            }
            String cacheKey = PluginCacheUtil.genCacheKey(psiMethod);
            String defaultJson = ParamUtil.getDefaultJson(psiMethod.getParameterList());
            CacheVo cache = PluginCacheUtil.getCacheOrDefault(psiMethod, project, defaultJson);
            bp = BreakpointUtil.addBreakpoint(e.getData(CommonDataKeys.PSI_FILE), project, psiMethod);
            if (Objects.isNull(bp)) {
                return;
            }
            bp.setConditionExpression(cache.getExpression());
            RuntimeTestDialog runtimeTestDialog = new RuntimeTestDialog(project, cacheKey, cache, defaultJson, bp);
//            Disposer.register(this, runtimeTestDialog.getDisposable());
            runtimeTestDialog.show();
            if (runtimeTestDialog.isOK()) {
                XLineBreakpoint<JavaMethodBreakpointProperties> finalBp = bp;
                CompletableFuture.runAsync(() -> RunUtil.run(project, cache, finalBp));
            }
        } catch (Exception exception) {
            BreakpointUtil.removeBreakpoint(project, bp);
            log.error("invoke exception", exception);
        }
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        PsiMethod method = PsiTreeUtil.getParentOfType(getElement(editor, file), PsiMethod.class);
        boolean enabled = project != null && editor != null && method != null;
        if (enabled && e.getDataContext() instanceof UserDataHolder) {
            ((UserDataHolder) e.getDataContext()).putUserData(USER_DATE_ELEMENT_KEY, method);
        }
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    @Nullable
    public static PsiElement getElement(Editor editor, PsiFile file) {
        if (editor == null || file == null) {
            return null;
        }
        CaretModel caretModel = editor.getCaretModel();
        int position = caretModel.getOffset();
        return file.findElementAt(position);
    }

    // idea version 243
//    @Override
//    public @NotNull ActionUpdateThread getActionUpdateThread() {
//        return ActionUpdateThread.BGT;
//    }

    @Override
    public void dispose() {

    }
}
