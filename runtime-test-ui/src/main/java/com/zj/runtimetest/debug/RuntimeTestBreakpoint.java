package com.zj.runtimetest.debug;

import com.intellij.debugger.DebuggerInvocationUtil;
import com.intellij.debugger.EvaluatingComputable;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.ContextUtil;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.engine.evaluation.expression.EvaluatorBuilderImpl;
import com.intellij.debugger.engine.evaluation.expression.ExpressionEvaluator;
import com.intellij.debugger.engine.evaluation.expression.UnsupportedExpressionException;
import com.intellij.debugger.engine.events.SuspendContextCommandImpl;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.debugger.ui.breakpoints.MethodBreakpoint;
import com.intellij.debugger.ui.impl.watch.CompilingEvaluatorImpl;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.zj.runtimetest.language.PluginBundle;
import com.zj.runtimetest.utils.BreakpointUtil;
import com.zj.runtimetest.utils.NoticeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author : jie.zhou
 * @date : 2025/7/10
 */
public class RuntimeTestBreakpoint extends MethodBreakpoint {
    private static final Logger log = LoggerFactory.getLogger(RuntimeTestBreakpoint.class);

    protected RuntimeTestBreakpoint(Project project, XBreakpoint xBreakpoint) {
        super(project, xBreakpoint);
    }

    @Override
    protected @NotNull JavaMethodBreakpointProperties getProperties() {
        return super.getProperties();
    }

    @Override
    public boolean processLocatableEvent(@NotNull SuspendContextCommandImpl action, LocatableEvent event) {
        boolean b = false;
        try {
            b = super.processLocatableEvent(action, event);
        } catch (Exception e) {
            log.error("[RuntimeTest] processLocatableEvent error: ", e);
        }
        BreakpointUtil.removeBreakpoint(getProject(), this.getXBreakpoint());
        return b;
    }

    @Override
    public boolean evaluateCondition(@NotNull EvaluationContextImpl context, @NotNull LocatableEvent event) {
        try {
//            SourcePosition contextSourcePosition = ContextUtil.getSourcePosition(context);
            SourcePosition sourcePosition = getSourcePosition();
            if (Objects.isNull(sourcePosition)) {
                log.error("[RuntimeTest] getSourcePosition is null");
                return false;
            }
            // 执行器判断位置定位到方法下一行，context的位置可能不准导致报错（修改代码没重新启动，这时行号变化，context还是老的）
            SourcePosition contextSourcePosition = SourcePosition.createFromLine(sourcePosition.getFile(), sourcePosition.getLine() + 1);
            ExpressionEvaluator evaluator = DebuggerInvocationUtil.commitAndRunReadAction(this.myProject, () -> {
                PsiElement contextElement = ContextUtil.getContextElement(contextSourcePosition);
                PsiElement contextPsiElement = contextElement != null ? contextElement : this.getEvaluationElement();
                return EvaluatorCache.cacheOrGet(event.request(), contextPsiElement, getCondition(),
                        () -> createExpressionEvaluator(getProject(), contextPsiElement, contextSourcePosition, getCondition(), this::createConditionCodeFragment)
                );
            });
            evaluator.evaluate(context);
            NoticeUtil.notice(this.getProject(), "[RuntimeTest] " + PluginBundle.get("evaluation.message.success"));
        } catch (Exception e) {
            log.error("[RuntimeTest] Pre-processing evaluate error: ", e);
        }
        return false;
    }

    /**
     * 以下拷贝自
     * @see com.intellij.debugger.ui.breakpoints.Breakpoint
     */
    private static ExpressionEvaluator createExpressionEvaluator(Project project, PsiElement contextPsiElement, SourcePosition contextSourcePosition, TextWithImports text, Function<? super PsiElement, ? extends PsiCodeFragment> fragmentFactory) throws EvaluateException {
        try {
            return EvaluatorBuilderImpl.build(text, contextPsiElement, contextSourcePosition, project);
        } catch (UnsupportedExpressionException var7) {
            ExpressionEvaluator eval = CompilingEvaluatorImpl.create(project, contextPsiElement, fragmentFactory);
            if (eval != null) {
                return eval;
            } else {
                throw var7;
            }
        } catch (IndexNotReadyException var8) {
            throw new EvaluateException(PluginBundle.get("evaluation.error.during.indexing"), var8);
        }
    }

    private PsiCodeFragment createConditionCodeFragment(PsiElement context) {
        TextWithImports text = this.getCondition();
        // idea version 243
//        return DebuggerUtilsEx.findAppropriateCodeFragmentFactory(text, context).createPsiCodeFragment(text, context, getProject());
        // idea version 223
        return DebuggerUtilsEx.findAppropriateCodeFragmentFactory(text, context).createCodeFragment(text, context, getProject());
    }

    private static final class EvaluatorCache {
        private final PsiElement myContext;
        private final TextWithImports myTextWithImports;
        private final ExpressionEvaluator myEvaluator;

        private EvaluatorCache(PsiElement context, TextWithImports textWithImports, ExpressionEvaluator evaluator) {
            this.myContext = context;
            this.myTextWithImports = textWithImports;
            this.myEvaluator = evaluator;
        }

        static @Nullable ExpressionEvaluator cacheOrGet(EventRequest request, PsiElement context, TextWithImports text, EvaluatingComputable<? extends ExpressionEvaluator> supplier) throws EvaluateException {
            String propertyName = "RuntimeTestEvaluator";
            EvaluatorCache cache = (EvaluatorCache) request.getProperty(propertyName);
            if (cache != null && Objects.equals(cache.myContext, context) && Objects.equals(cache.myTextWithImports, text)) {
                return cache.myEvaluator;
            } else {
                ExpressionEvaluator evaluator = supplier.compute();
                request.putProperty(propertyName, new EvaluatorCache(context, text, evaluator));
                return evaluator;
            }
        }
    }
}
