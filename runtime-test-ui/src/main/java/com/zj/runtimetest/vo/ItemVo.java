package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : jie.zhou
 * @date : 2025/8/6
 */
@Data
@AllArgsConstructor
public class ItemVo<T> {

    private Integer index;

    private T value;

}
