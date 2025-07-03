package com.zj.runtimetest.test;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.zj.runtimetest.test.language.PluginBundle;
import com.zj.runtimetest.test.utils.MethodUtil;
import com.zj.runtimetest.test.utils.NoticeUtil;
import com.zj.runtimetest.test.utils.ParamUtil;
import com.zj.runtimetest.test.utils.PluginCacheUtil;
import com.zj.runtimetest.test.vo.CacheVo;
import com.zj.runtimetest.utils.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Objects;



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
//            if (!MethodUtil.isPublicMethod(psiMethod)) {
//                NoticeUtil.error(project, PluginBundle.get("notice.error.method-not-public"));
//                return;
//            }
            String cacheKey = PluginCacheUtil.genCacheKey(psiMethod);
            String defaultJson = ParamUtil.getDefaultJson(psiMethod.getParameterList());
            CacheVo cache = PluginCacheUtil.getCache(psiMethod);
            if (Objects.isNull(cache)) {
                cache = new CacheVo();
                PsiParameterList parameterList = psiMethod.getParameterList();
                cache.setClassName(((PsiClass) psiMethod.getParent()).getQualifiedName());
                cache.setMethodName(psiMethod.getName());
                cache.setParameterTypeList(ParamUtil.getParamTypeNameList(parameterList));
                cache.setProjectBasePath(project.getBasePath());
                cache.setStaticMethod(MethodUtil.isStaticMethod(psiMethod));
                cache.setRequestJson(defaultJson);
            }
            CacheVo cacheVo = cache;
            RuntimeTestDialog runtimeTestDialog = new RuntimeTestDialog(project, cacheKey, cacheVo, defaultJson);
//            Disposer.register(this, runtimeTestDialog.getDisposable());
            runtimeTestDialog.show();
            if (!runtimeTestDialog.isOK()) {
                return;
            }
            run(project, cacheVo);
        } catch (Exception exception) {
            log.error("invoke exception", exception);
        }
    }

    private void run(Project project, CacheVo cache) {
        String coreJarPath = PathManager.getPluginsPath() + File.separator + "runtime-test-ui" + File.separator + "lib" + File.separator + "runtime-test-core.jar";
        String pid = cache.getPid().toString();
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            vm.loadAgent(coreJarPath, JsonUtil.toJsonString(cache));
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Non-numeric value found")) {
                log.warn("jdk lower version attach higher version, can ignore");
            } else {
                if (Objects.equals(e.getMessage(), "No such process")) {
                    NoticeUtil.error(project, PluginBundle.get("notice.error.no-such-process") + " " + pid);
                } else {
                    log.error("e: ", e);
                    NoticeUtil.error(project, e.getMessage());
                }
            }
        } catch (AgentLoadException e) {
            if ("0".equals(e.getMessage())) {
                log.warn("jdk higher version attach lower version, can ignore");
            } else {
                log.error("e: ", e);
                NoticeUtil.error(project, e.getMessage());
            }
        } catch (Exception e) {
            log.error("e: ", e);
            NoticeUtil.error(project, e.getMessage());
        } finally {
            if (null != vm) {
                try {
                    vm.detach();
                } catch (Exception ignored) {
                }
            }
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

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void dispose() {

    }
}
