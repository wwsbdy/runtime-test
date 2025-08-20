package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表达式信息
 * @author 19242
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionVo {
    private String myExpression;
    private String myCustomInfo;
}
