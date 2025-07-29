package com.zj.runtimetest.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author : jie.zhou
 * @date : 2025/6/30
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class CacheVo extends RequestInfo implements Serializable {

    private static final long serialVersionUID = -574377082031667607L;

    @JsonIgnore
    private Long pid;

    @JsonIgnore
    private final int size = 5;

    @JsonIgnore
    private List<String> history;


    public void addHistory(String s) {
        if (Objects.isNull(history)) {
            history = new ArrayList<>();
        }
        if (history.contains(s)) {
            history.remove(s);
            history.add(s);
            return;
        }
        if (history.size() >= size) {
            history.remove(history.get(0));
        }
        history.add(s);
    }

    public void forEachHistory(Consumer<String> action) {
        if (Objects.isNull(history)) {
            history = new ArrayList<>();
        }
        // 倒序
        ArrayList<String> list = new ArrayList<>(history);
        for (int i = list.size() - 1; i >= 0; i--) {
            action.accept(list.get(i));
        }
    }

    public void setExpression(@NotNull XExpression expression) {
        super.setExpVo(fromExpression(expression));
    }

    @JsonIgnore
    public @NotNull XExpression getExpression() {
        if (Objects.isNull(getExpVo())) {
            return EmptyXExpression.INSTANCE;
        }
        return toExpression(getExpVo());
    }

    private XExpression toExpression(ExpressionVo expVo) {
        if (Objects.isNull(expVo)) {
            return EmptyXExpression.INSTANCE;
        }
        return new XExpressionImpl(expVo.getMyExpression(), JavaLanguage.INSTANCE, expVo.getMyCustomInfo(), EvaluationMode.CODE_FRAGMENT);
    }

    private ExpressionVo fromExpression(XExpression expression) {
        if (StringUtils.isBlank(expression.getExpression())) {
            return null;
        }
        ExpressionVo vo = new ExpressionVo();
        vo.setMyExpression(expression.getExpression());
        vo.setMyCustomInfo(expression.getCustomInfo());
        return vo;
    }

    public static class EmptyXExpression {
        public static final XExpression INSTANCE;

        static {
            INSTANCE = new XExpressionImpl("", JavaLanguage.INSTANCE, null, EvaluationMode.CODE_FRAGMENT);
//            INSTANCE = XExpressionImpl.fromText("", EvaluationMode.CODE_FRAGMENT);
        }
    }


}
