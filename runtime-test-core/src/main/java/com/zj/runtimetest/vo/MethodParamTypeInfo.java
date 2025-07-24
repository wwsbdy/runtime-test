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

    public MethodParamTypeInfo(String paramName, String paramType, Type type) {
        super(paramName, paramType);
        this.type = type;
    }

    public MethodParamTypeInfo(Type type) {
        this.type = type;
    }
}
