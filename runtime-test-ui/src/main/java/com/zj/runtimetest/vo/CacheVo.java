package com.zj.runtimetest.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.xdebugger.XExpression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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

    @JsonIgnore
    private ExpressionVo expressionVo;


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
        this.expressionVo = ExpressionVo.fromExpression(expression);
    }

    @JsonIgnore
    public @NotNull XExpression getExpression() {
        if (Objects.isNull(expressionVo)) {
            return ExpressionVo.EmptyXExpression.INSTANCE;
        }
        return expressionVo.toExpression();
    }


}
