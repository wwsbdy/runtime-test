package com.zj.runtimetest.vo;

import lombok.Data;

/**
 * @author : jie.zhou
 * @date : 2025/8/20
 */
@Data
public class Result {

    public static final Result FAIL = new Result();

    private Object result;
    private boolean success;

    public Result(Object result) {
        this.result = result;
        this.success = true;
    }

    private Result() {
    }
}
