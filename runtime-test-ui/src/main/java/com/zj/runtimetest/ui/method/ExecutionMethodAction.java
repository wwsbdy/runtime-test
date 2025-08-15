package com.zj.runtimetest.ui.method;

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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.utils.MethodUtil;
import com.zj.runtimetest.utils.NoticeUtil;
import com.zj.runtimetest.utils.ParamUtil;
import com.zj.runtimetest.utils.PluginCacheUtil;
import com.zj.runtimetest.vo.CacheAndKeyVo;
import com.zj.runtimetest.vo.CacheVo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


/**
 * Original work: AnyDoorOpenAction - Copyright (c) 2025 [lgp547]
 * Modifications/adjustments made by [jie.zhou] on [2025/6/30].
 * Key modifications:
 * 1. [调整调用窗口]
 * 2. [调整请求信息]
 * Source: <a href="https://github.com/lgp547/any-door">any-door</a>
 * License: [Apache 2.0]
 *
 * @author jie.zhou
 * @date : 2025/7/2
 */
public class ExecutionMethodAction extends AnAction implements Disposable {

    private static final Logger log = Logger.getInstance(ExecutionMethodAction.class);

    private final static Key<PsiMethod> USER_DATE_ELEMENT_KEY = new Key<>("user.psi.Element");

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project project = e.getProject();
        try {
            PsiMethod psiMethod = getPsiMethod(e);
            if (MethodUtil.isConstructor(psiMethod)) {
                NoticeUtil.notice(project, PluginBundle.get("notice.info.method-constructor"));
                return;
            }
            String defaultJson = ParamUtil.getDefaultJson(psiMethod.getParameterList());

            CacheAndKeyVo cacheAndKeyVo = PluginCacheUtil.getCacheOrDefault(psiMethod, project, defaultJson);
            String cacheKey = cacheAndKeyVo.getCacheKey();
            CacheVo cache = cacheAndKeyVo.getCache();
            ExecutionMethodDialog executionMethodDialog = new ExecutionMethodDialog(project, cacheKey, cache, defaultJson, psiMethod);
//            Disposer.register(this, runtimeTestDialog.getDisposable());
            executionMethodDialog.show();
        } catch (Exception exception) {
            log.error("invoke exception", exception);
        }
    }

    protected @NotNull PsiMethod getPsiMethod(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (Objects.isNull(editor)) {
            throw new IllegalArgumentException("idea arg error (project or editor is null)");
        }
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
        return psiMethod;
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        PsiMethod method = PsiTreeUtil.getParentOfType(getElement(editor, file), PsiMethod.class);
        boolean enabled = project != null && editor != null && method != null;
        if (!enabled || disabledMethod(method)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        if (e.getDataContext() instanceof UserDataHolder) {
            ((UserDataHolder) e.getDataContext()).putUserData(USER_DATE_ELEMENT_KEY, method);
        }
        e.getPresentation().setEnabledAndVisible(true);
    }

    /**
     * 禁用方法
     *
     * @param psiMethod 方法
     * @return true: 禁用
     */
    protected boolean disabledMethod(@NotNull PsiMethod psiMethod) {
        // 过滤构造器
        if (MethodUtil.isConstructor(psiMethod)) {
            return true;
        }
        PsiElement parent = psiMethod.getParent();
        // 过滤匿名类
        if (parent instanceof PsiAnonymousClass) {
            return true;
        }
        // 过滤本地类（定义在方法内部的类）
        return parent instanceof PsiDeclarationStatement;
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
