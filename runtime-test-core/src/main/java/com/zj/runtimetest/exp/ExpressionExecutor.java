package com.zj.runtimetest.exp;

import com.zj.runtimetest.AgentContextHolder;
import com.zj.runtimetest.utils.HttpServletRequestUtil;
import com.zj.runtimetest.utils.LogUtil;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/7/31
 */
@Getter(AccessLevel.PACKAGE)
@EqualsAndHashCode
public abstract class ExpressionExecutor {
    @Setter(AccessLevel.PACKAGE)
    private String classStr;
    private Map<String, Object> headers;
    private Map<String, Object> attributes;

    public abstract Object[] eval(Object[] args);

    protected void fakeMethod(Object... args) {
        LogUtil.alwaysErr("[Agent] Don not execute me");
    }

    protected void printPreProcessingMethod() {
        if (Objects.nonNull(classStr) && !classStr.isEmpty()) {
            LogUtil.alwaysLog(classStr);
        }
    }

    protected void addHeader(String name, Object value) {
        if (!HttpServletRequestUtil.hasHttpServletRequest()) {
            LogUtil.alwaysErr("[Agent] java.lang.ClassNotFoundException: javax.servlet.http.HttpServletRequest");
            return;
        }
        if (Objects.isNull(headers)) {
            headers = new LinkedHashMap<>();
        }
        headers.put(name, value);
    }

    protected void setAttribute(String name, Object value) {
        if (!HttpServletRequestUtil.hasHttpServletRequest()) {
            LogUtil.alwaysErr("[Agent] java.lang.ClassNotFoundException: javax.servlet.http.HttpServletRequest");
            return;
        }
        if (Objects.isNull(attributes)) {
            attributes = new LinkedHashMap<>();
        }
        attributes.put(name, value);
    }

    protected void printBegin() {
        LogUtil.log("[Agent more] pre-processing begin");
    }

    protected void printEnd() {
        LogUtil.log("[Agent more] pre-processing end");
    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(Class<T> clz) {
        try {
            return (T) AgentContextHolder.getBean(clz.getName()).getBean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
