package com.zj.runtimetest.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.zj.runtimetest.debug.RuntimeTestBreakpointType;
import com.zj.runtimetest.language.PluginBundle;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties;

import java.util.Objects;

/**
 * @author 19242
 */
public class BreakpointUtil {
    public static @Nullable XLineBreakpoint<JavaMethodBreakpointProperties> addBreakpoint(PsiFile psiFile, Project project, PsiMethod psiMethod) {
        if (psiFile == null) {
            NoticeUtil.notice(project, "[RuntimeTest] " + PluginBundle.get("notice.info.preHandleInvalid"));
            return null;
        }
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            NoticeUtil.notice(project, "[RuntimeTest] " + PluginBundle.get("notice.info.preHandleInvalid"));
            return null;
        }
        Integer lineNumber = findFirstExecutableLine(psiMethod, project);
        if (Objects.isNull(lineNumber)) {
            NoticeUtil.notice(project, "[RuntimeTest] " + PluginBundle.get("notice.info.preHandleInvalid"));
            return null;
        }
        VirtualFile file = psiFile.getVirtualFile();

        RuntimeTestBreakpointType type = XDebuggerUtil.getInstance()
                .findBreakpointType(RuntimeTestBreakpointType.class);
        XDebuggerUtil.getInstance().canPutBreakpointAt(project, file, lineNumber);

        XBreakpointManager manager = XDebuggerManager.getInstance(project).getBreakpointManager();
        for (XLineBreakpoint<JavaMethodBreakpointProperties> bp : manager.getBreakpoints(type)) {
            if (bp.getFileUrl().equals(file.getUrl()) && bp.getLine() == lineNumber) {
                return bp;
            }
        }
        return manager.addLineBreakpoint(type, file.getUrl(), lineNumber, type.createProperties());
    }

    public static void removeBreakpoints(Project project) {
        RuntimeTestBreakpointType type = XDebuggerUtil.getInstance()
                .findBreakpointType(RuntimeTestBreakpointType.class);
        XBreakpointManager manager = XDebuggerManager.getInstance(project).getBreakpointManager();
        manager.getBreakpoints(type).forEach(manager::removeBreakpoint);
    }

    public static void removeBreakpoint(Project project, XBreakpoint<?> bp) {
        XBreakpointManager manager = XDebuggerManager.getInstance(project).getBreakpointManager();
        manager.removeBreakpoint(bp);
    }

    public static Integer findFirstExecutableLine(PsiMethod method, Project project) {
        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return null;
        }

        VirtualFile file = method.getContainingFile().getVirtualFile();
        if (file == null) {
            return null;
        }

        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return null;
        }
        int offset = method.getTextOffset();
        return document.getLineNumber(offset);

//        for (PsiStatement stmt : body.getStatements()) {
//            PsiElement element = findFirstValidBreakpointElement(stmt, project, file, document);
//            if (element == null) {
//                continue;
//            }
//
//            int offset = element.getTextOffset();
//            int line = document.getLineNumber(offset);
//
//            if (XDebuggerUtil.getInstance().canPutBreakpointAt(project, file, line)) {
//                return line;
//            }
//        }
//
//        return null;
    }

    private static @Nullable PsiElement findFirstValidBreakpointElement(
            PsiElement element,
            Project project,
            VirtualFile file,
            Document document
    ) {
        if (element instanceof PsiWhiteSpace || element instanceof PsiComment || element instanceof PsiEmptyStatement) {
            return null;
        }

        if (element instanceof PsiDeclarationStatement) {
            PsiDeclarationStatement declStmt = (PsiDeclarationStatement) element;
            for (PsiElement declared : declStmt.getDeclaredElements()) {
                if (declared instanceof PsiVariable && ((PsiVariable) declared).getInitializer() != null) {
                    return declared;
                }
            }
            return null;
        }

        if (element instanceof PsiBlockStatement) {
            PsiBlockStatement blockStmt = (PsiBlockStatement) element;
            for (PsiStatement stmt : blockStmt.getCodeBlock().getStatements()) {
                PsiElement result = findFirstValidBreakpointElement(stmt, project, file, document);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        if (element instanceof PsiIfStatement) {
            PsiIfStatement ifStmt = (PsiIfStatement) element;
            PsiStatement thenStmt = ifStmt.getThenBranch();
            if (thenStmt != null) {
                return findFirstValidBreakpointElement(thenStmt, project, file, document);
            }
            return null;
        }

        if (element instanceof PsiLoopStatement) {
            PsiLoopStatement loopStmt = (PsiLoopStatement) element;
            PsiStatement body = loopStmt.getBody();
            if (body != null) {
                return findFirstValidBreakpointElement(body, project, file, document);
            }
            return null;
        }

        if (element instanceof PsiTryStatement) {
            PsiTryStatement tryStmt = (PsiTryStatement) element;
            PsiCodeBlock tryBlock = tryStmt.getTryBlock();
            if (tryBlock != null) {
                for (PsiStatement stmt : tryBlock.getStatements()) {
                    PsiElement result = findFirstValidBreakpointElement(stmt, project, file, document);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }

        int offset = element.getTextOffset();
        int line = document.getLineNumber(offset);
        if (XDebuggerUtil.getInstance().canPutBreakpointAt(project, file, line)) {
            return element;
        }

        return null;
    }
}
