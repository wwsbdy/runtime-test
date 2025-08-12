package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author : jie.zhou
 * @date : 2025/8/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamVo {

    private String className;
    private String beanName;
    private Set<String> importNames;
}
