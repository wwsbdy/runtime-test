package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : jie.zhou
 * @date : 2025/8/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamVo {

    private String className = "Object";
    private String beanName = "bean";
    private String importName = "";
}
