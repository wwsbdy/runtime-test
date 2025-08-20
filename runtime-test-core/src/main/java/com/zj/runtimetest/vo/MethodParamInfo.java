package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方法参数信息
 * @author 19242
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodParamInfo {

    private String paramName;
    private String paramType;

}
