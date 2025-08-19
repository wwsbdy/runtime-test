package com.zj.runtimetest.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Type;

/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MethodParamTypeInfo extends MethodParamInfo {

    private Type type;
    private Class<?> cls;

    public MethodParamTypeInfo(String paramName, String paramType, Class<?> cls) {
        super(paramName, paramType);
        this.cls = cls;
    }

    public MethodParamTypeInfo(String paramName, String paramType, Class<?> cls, Type type) {
        super(paramName, paramType);
        this.cls = cls;
        this.type = type;
    }
}
