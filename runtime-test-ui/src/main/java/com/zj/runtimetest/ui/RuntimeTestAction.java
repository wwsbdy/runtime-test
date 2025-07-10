package com.zj.runtimetest.ui;

import com.google.common.base.Charsets;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.zj.runtimetest.debug.RuntimeTestBreakpointProperties;
import com.zj.runtimetest.debug.RuntimeTestBreakpointType;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.utils.*;
import com.zj.runtimetest.vo.CacheVo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
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
        XLineBreakpoint<RuntimeTestBreakpointProperties> bp = null;
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
            bp = addBreakpoint(e, project, psiMethod);
            if (Objects.isNull(bp)) {
                NoticeUtil.error(project, "[RuntimeTest] Add breakpoint failed");
                return;
            }
            bp.setConditionExpression(cache.getExpression());
            RuntimeTestDialog runtimeTestDialog = new RuntimeTestDialog(project, cacheKey, cache, defaultJson, bp);
//            Disposer.register(this, runtimeTestDialog.getDisposable());
            runtimeTestDialog.show();
            if (!runtimeTestDialog.isOK()) {
                return;
            }
            run(project, cache);
        } catch (Exception exception) {
            if (Objects.nonNull(bp)) {
                XDebuggerManager.getInstance(project).getBreakpointManager().removeBreakpoint(bp);
            }
            log.error("invoke exception", exception);
        }
    }

    private @Nullable XLineBreakpoint<RuntimeTestBreakpointProperties> addBreakpoint(AnActionEvent e, Project project, PsiMethod psiMethod) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return null;
        }
        int startLineNumber = document.getLineNumber(psiMethod.getTextOffset());
        int endLineNumber = document.getLineNumber(psiMethod.getTextRange().getEndOffset() - 1);
        // TODO 方法名的下一行如果是空或try这些，debug就无效了
        int lineNumber = startLineNumber >= endLineNumber ? startLineNumber : startLineNumber + 1;
        VirtualFile file = psiFile.getVirtualFile();

        RuntimeTestBreakpointType type = XDebuggerUtil.getInstance()
                .findBreakpointType(RuntimeTestBreakpointType.class);

        XBreakpointManager manager = XDebuggerManager.getInstance(project).getBreakpointManager();
        for (XLineBreakpoint<RuntimeTestBreakpointProperties> bp : manager.getBreakpoints(type)) {
            if (bp.getFileUrl().equals(file.getUrl()) && bp.getLine() == lineNumber) {
                return bp;
            }
        }
        return manager.addLineBreakpoint(type, file.getUrl(), lineNumber, type.createProperties());
    }
    private void run(Project project, CacheVo cache) {
        String coreJarPath = PathManager.getPluginsPath() + File.separator + "runtime-test-ui" + File.separator + "lib" + File.separator + "runtime-test-core.jar";
        String pid = cache.getPid().toString();
        String requestJson = JsonUtil.toJsonString(cache);
        String jsonPath = project.getBasePath() + "/.idea/runtime-test/RequestInfo.json";
        if (requestJson.length() > 600 && flushFile(jsonPath, requestJson)) {
            requestJson = "file://" + URLEncoder.encode(jsonPath, Charsets.UTF_8);
        }
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            vm.loadAgent(coreJarPath, requestJson);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Non-numeric value found")) {
                log.warn("jdk lower version attach higher version, can ignore");
            } else {
                if (Objects.equals(e.getMessage(), "No such process")) {
                    NoticeUtil.error(project, PluginBundle.get("[RuntimeTest] notice.error.no-such-process") + " " + pid);
                } else {
                    log.error("e: ", e);
                    NoticeUtil.error(project, "[RuntimeTest] " + ThrowUtil.printStackTrace(e));
                }
            }
        } catch (AgentLoadException e) {
            if ("0".equals(e.getMessage())) {
                log.warn("jdk higher version attach lower version, can ignore");
            } else {
                log.error("e: ", e);
                NoticeUtil.error(project, "[RuntimeTest] " + ThrowUtil.printStackTrace(e));
            }
        } catch (Exception e) {
            log.error("e: ", e);
            NoticeUtil.error(project, "[RuntimeTest] " + ThrowUtil.printStackTrace(e));
        } finally {
            if (null != vm) {
                try {
                    vm.detach();
                } catch (Exception ignored) {
                }
            }
        }
    }


    /**
     * rewrite content to file
     */
    public static boolean flushFile(String filePath, String content) {
        File file = new File(filePath);
        try {
            FileUtil.writeToFile(file, content);
            return true;
        } catch (IOException e) {
            log.error("flushFile error [filePath:{} content:{} errMsg:{}]", filePath, content, e.getMessage());
        }
        return false;
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
