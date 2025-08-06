package com.zj.runtimetest.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.util.xmlb.annotations.Transient;
import com.intellij.xdebugger.XExpression;
import com.zj.runtimetest.constant.Constant;
import com.zj.runtimetest.utils.ExpressionUtil;
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

    /**
     * 进程id
     * 不缓存
     */
    @JsonIgnore
    private Long pid;

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
        if (history.size() >= Constant.HISTORY_SIZE) {
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
        super.setExpVo(ExpressionUtil.fromExpression(expression));
    }

    @JsonIgnore
    public @NotNull XExpression getExpression() {
        if (Objects.isNull(getExpVo())) {
            return ExpressionUtil.EmptyXExpression.INSTANCE;
        }
        return ExpressionUtil.toExpression(getExpVo());
    }


    @Transient
    @Override
    public boolean isStaticMethod() {
        return super.isStaticMethod();
    }

    @Transient
    @Override
    public String getProjectBasePath() {
        return super.getProjectBasePath();
    }

    @Transient
    public Long getPid() {
        return pid;
    }

    @Transient
    @Override
    public String getClassName() {
        return super.getClassName();
    }

    @Transient
    @Override
    public String getMethodName() {
        return super.getMethodName();
    }

    @Transient
    @Override
    public List<MethodParamInfo> getParameterTypeList() {
        return super.getParameterTypeList();
    }

    @Transient
    @Override
    public String getRequestJson() {
        return super.getRequestJson();
    }

    @Transient
    @Override
    public void setDetailLog(boolean detailLog) {
        super.setDetailLog(detailLog);
    }
}
