package com.zj.runtimetest.ui.script;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.ui.method.ExecutionMethodAction;
import com.zj.runtimetest.utils.ExpressionUtil;
import com.zj.runtimetest.utils.MethodUtil;
import com.zj.runtimetest.utils.NoticeUtil;
import com.zj.runtimetest.utils.ParamUtil;
import com.zj.runtimetest.vo.ExpressionVo;
import com.zj.runtimetest.vo.ParamVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


/**
 * 脚本
 * @author : jie.zhou
 * @date : 2025/8/12
 */
public class ScriptAction extends ExecutionMethodAction {

    private static final Logger log = Logger.getInstance(ScriptAction.class);

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project project = e.getProject();
        if (null == project) {
            throw new IllegalArgumentException("idea arg error (project or editor is null)");
        }
        ToolWindow runtimeTest = ToolWindowManager.getInstance(project).getToolWindow("RuntimeTest");
        if (Objects.isNull(runtimeTest)) {
            NoticeUtil.notice(project, PluginBundle.get("notice.info.tool-window-error"));
            return;
        }
        try {
            PsiMethod psiMethod = getPsiMethod(e);
            ExpressionVo expressionVo = ExpressionUtil.getDefaultExpression(psiMethod);
            addInvokeMethod(expressionVo, psiMethod);
            ScriptToolWindowFactory.addContent(project, runtimeTest, expressionVo);
            runtimeTest.show();
        } catch (Exception exception) {
            log.error("invoke exception", exception);
        }
    }


    /**
     * 添加调用方法
     *
     * @param expressionVo 表达式信息
     * @param psiMethod    方法信息
     */
    private void addInvokeMethod(@NotNull ExpressionVo expressionVo, @NotNull PsiMethod psiMethod) {
        Set<String> paramNames = new HashSet<>();
        PsiParameterList parameterList = psiMethod.getParameterList();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            paramNames.add(parameter.getName());
        }
        StringBuilder expression = new StringBuilder();
        StringBuilder imports = new StringBuilder();
        if (StringUtils.isNotBlank(expressionVo.getMyExpression())) {
            expression.append(expressionVo.getMyExpression());
            expression.append("\n");
        }
        if (StringUtils.isNotBlank(expressionVo.getMyCustomInfo())) {
            imports.append(expressionVo.getMyCustomInfo());
        }
        ParamVo paramVo = ParamUtil.getParamVo((PsiClass) psiMethod.getParent());
        String className = paramVo.getClassName();
        String beanName = paramVo.getBeanName();
        Set<String> importNames = paramVo.getImportNames();
        if (CollectionUtils.isNotEmpty(importNames)) {
            importNames.forEach(importName -> imports.append(importName).append(","));
        }
        if (MethodUtil.isStaticMethod(psiMethod)) {
            beanName = className;
        } else {
            // 兼容bean名称和属性名冲突
            if (paramNames.contains(beanName)) {
                beanName = beanName + "_" + Integer.toHexString(Math.abs(UUID.randomUUID().toString().hashCode()));
            }
            paramNames.add(beanName);
            expression.append("// ").append(PluginBundle.get("description.get-bean")).append("\n");
            expression.append(className).append(" ").append(beanName).append(" = getBean(").append(className).append(".class);\n");
        }
        expression.append("// ").append(PluginBundle.get("description.invoke-method")).append("\n");
        PsiType returnType = psiMethod.getReturnType();
        String resultBeanName = null;
        if (Objects.nonNull(returnType) && !"void".equals(returnType.getCanonicalText())) {
            ParamVo resultParam = ParamUtil.getParamVo(returnType);
            String resultClassName = resultParam.getClassName();
            Set<String> resultImportNames = resultParam.getImportNames();
            resultBeanName = resultParam.getBeanName();
            // 兼容返回属性名冲突
            if (paramNames.contains(resultBeanName)) {
                resultBeanName = resultBeanName + "_" + Integer.toHexString(Math.abs(UUID.randomUUID().toString().hashCode()));
            }
            paramNames.add(resultBeanName);
            if (CollectionUtils.isNotEmpty(resultImportNames)) {
                resultImportNames.forEach(importName -> imports.append(importName).append(","));
            }
            expression.append(resultClassName).append(" ").append(resultBeanName).append(" = ");
        }
        expression.append(beanName).append(".").append(psiMethod.getName()).append("(");
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            expression.append(parameter.getName());
            if (i != parameterList.getParametersCount() - 1) {
                expression.append(", ");
            }
        }
        expression.append(");");
        if (Objects.nonNull(resultBeanName)) {
            expression.append("\nSystem.out.println(toJsonString(").append(resultBeanName).append("));");
        }
        expressionVo.setMyExpression(expression.toString());
        expressionVo.setMyCustomInfo(imports.toString());
    }

    @Override
    protected boolean disabledMethod(@NotNull PsiMethod psiMethod) {
        // 脚本只支持public的方法
        if (!MethodUtil.isPublicMethod(psiMethod)) {
            return true;
        }
        return super.disabledMethod(psiMethod);
    }

}
