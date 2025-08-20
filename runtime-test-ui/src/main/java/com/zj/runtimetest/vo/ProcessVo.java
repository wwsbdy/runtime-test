package com.zj.runtimetest.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 进程信息
 * @author : jie.zhou
 * @date : 2025/7/8
 */
@Data
@AllArgsConstructor
public class ProcessVo {

    private Long pid;

    private String env;

    private Long executionId;

    private String executorId;

}
