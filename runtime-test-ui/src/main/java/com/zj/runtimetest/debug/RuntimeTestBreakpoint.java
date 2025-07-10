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
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.debugger.ui.impl.watch.CompilingEvaluatorImpl;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElement;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.zj.runtimetest.language.PluginBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author : jie.zhou
 * @date : 2025/7/10
 */
public class RuntimeTestBreakpoint extends LineBreakpoint<RuntimeTestBreakpointProperties> {
    private static final Logger log = LoggerFactory.getLogger(RuntimeTestBreakpoint.class);

    protected RuntimeTestBreakpoint(Project project, XBreakpoint xBreakpoint) {
        super(project, xBreakpoint);
    }

    @Override
    protected @NotNull RuntimeTestBreakpointProperties getProperties() {
        return super.getProperties();
    }

    @Override
    public boolean processLocatableEvent(@NotNull SuspendContextCommandImpl action, LocatableEvent event) {
        boolean b = false;
        try {
            b = super.processLocatableEvent(action, event);
        } catch (Exception e) {
            log.error("processLocatableEvent error: ", e);
        }
        XDebuggerManager.getInstance(getProject()).getBreakpointManager().removeBreakpoint(this.getXBreakpoint());
        return b;
    }

    @Override
    public boolean evaluateCondition(EvaluationContextImpl context, LocatableEvent event) {
        try {
            SourcePosition contextSourcePosition = ContextUtil.getSourcePosition(context);
            ExpressionEvaluator evaluator = DebuggerInvocationUtil.commitAndRunReadAction(this.myProject, () -> {
                PsiElement contextElement = ContextUtil.getContextElement(contextSourcePosition);
                PsiElement contextPsiElement = contextElement != null ? contextElement : this.getEvaluationElement();
                return EvaluatorCache.cacheOrGet(event.request(), contextPsiElement, getCondition(),
                        () -> createExpressionEvaluator(getProject(), contextPsiElement, contextSourcePosition, getCondition(), this::createConditionCodeFragment)
                );
            });
            evaluator.evaluate(context);
        } catch (Exception e) {
            log.error("evaluate error: ", e);
        }
        return false;
    }

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
        // org.jetbrains.intellij version 1.14.1
//        return DebuggerUtilsEx.findAppropriateCodeFragmentFactory(text, context).createPsiCodeFragment(text, context, getProject());
        // org.jetbrains.intellij version 1.0
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
