package com.zj.runtimetest.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.zj.runtimetest.utils.BreakpointUtil;
import com.zj.runtimetest.utils.ParamUtil;
import com.zj.runtimetest.utils.PluginCacheUtil;
import com.zj.runtimetest.vo.CacheVo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties;

import java.util.Optional;
import java.util.function.Function;


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
        try {
            PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
            PsiMethod psiMethod = null;
            if (e.getDataContext() instanceof UserDataHolder) {
                psiMethod = ((UserDataHolder) e.getDataContext()).getUserData(USER_DATE_ELEMENT_KEY);
            }
            if (psiMethod == null) {
                psiMethod = PsiTreeUtil.getParentOfType(getElement(editor, file), PsiMethod.class);
                if (psiMethod == null) {
                    throw new IllegalArgumentException("idea arg error (method is null)");
                }
            }
            Integer lineNumber = BreakpointUtil.findFirstExecutableLine(psiMethod, project);
            String fileUrl = Optional.ofNullable(file).map(PsiFile::getVirtualFile).map(VirtualFile::getUrl).orElse(null);
            String cacheKey = PluginCacheUtil.genCacheKey(psiMethod);
            String defaultJson = ParamUtil.getDefaultJson(psiMethod.getParameterList());
            CacheVo cache = PluginCacheUtil.getCacheOrDefault(psiMethod, project, defaultJson);
            Function<Boolean, XLineBreakpoint<JavaMethodBreakpointProperties>> breakpointFunc =
                    addIfAbsent -> BreakpointUtil.addBreakpoint(project, fileUrl, lineNumber, addIfAbsent);
            RuntimeTestDialog runtimeTestDialog = new RuntimeTestDialog(project, cacheKey, cache, defaultJson, breakpointFunc,
                    Optional.ofNullable(file).map(PsiFile::getVirtualFile).orElse(null), lineNumber);
//            Disposer.register(this, runtimeTestDialog.getDisposable());
            runtimeTestDialog.show();
        } catch (Exception exception) {
            ApplicationManager.getApplication()
                    .runWriteAction(() ->
                            BreakpointUtil.removeBreakpoints(project)
                    );
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
