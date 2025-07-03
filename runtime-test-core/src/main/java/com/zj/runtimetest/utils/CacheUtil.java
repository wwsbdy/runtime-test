package com.zj.runtimetest.utils;

import com.zj.runtimetest.vo.MethodParamInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jie.zhou
 */
public class CacheUtil {

    public static String genCacheKey(String className, String methodName, List<MethodParamInfo> paramTypeList) {
        return className + "#" + methodName + "#" + paramTypeList.stream()
                .map(MethodParamInfo::getParamType)
                .collect(Collectors.joining(","));
    }
}
