package com.zj.runtimetest.exp;

import com.zj.runtimetest.AgentContextHolder;
import com.zj.runtimetest.utils.HttpServletRequestUtil;
import com.zj.runtimetest.utils.JsonUtil;
import com.zj.runtimetest.utils.LogUtil;
import com.zj.runtimetest.vo.IHttpServletRequest;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.Objects;

/**
 * 执行器抽象类
 * @author : jie.zhou
 * @date : 2025/7/31
 */
@Setter
@EqualsAndHashCode
public abstract class ExpressionExecutor {

    /**
     * 源代码
     */
    private String sourceCode;

    public abstract Object[] eval(Object[] args);

    protected void fakeMethod(Object... args) {
        LogUtil.alwaysErr("[Agent] Don not execute me");
    }

    protected void printPreProcessingMethod() {
        if (Objects.nonNull(sourceCode) && !sourceCode.isEmpty()) {
            LogUtil.alwaysLog(sourceCode);
        }
    }

    protected void addHeader(String name, Object value) {
        if (!HttpServletRequestUtil.hasHttpServletRequest()) {
            LogUtil.alwaysErr("[Agent] addHeader: java.lang.ClassNotFoundException: javax.servlet.http.HttpServletRequest");
            return;
        }
        HttpServletRequestUtil.addHeader(name, value);
    }

    protected void setAttribute(String name, Object value) {
        if (!HttpServletRequestUtil.hasHttpServletRequest()) {
            LogUtil.alwaysErr("[Agent] setAttribute: java.lang.ClassNotFoundException: javax.servlet.http.HttpServletRequest");
            return;
        }
        HttpServletRequestUtil.setAttribute(name, value);
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

    protected IHttpServletRequest getHttpServletRequest() {
        return HttpServletRequestUtil.getHttpServletRequest();
    }

    protected String toJsonString(Object value) {
        return JsonUtil.toJsonString(value);
    }

    protected Object getBean(String name) {
        try {
            Object bean = AgentContextHolder.getBeanByName(name);
            if (Objects.isNull(bean)) {
                LogUtil.alwaysErr("[Agent] getBean: java.lang.ClassNotFoundException: " + name);
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getBean(String name, Class<T> clz) {
        try {
            T bean = (T) AgentContextHolder.getBeanByName(name, clz.getName());
            if (Objects.isNull(bean)) {
                LogUtil.alwaysErr("[Agent] getBean: java.lang.ClassNotFoundException: " + name + ", class: " + clz);
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
