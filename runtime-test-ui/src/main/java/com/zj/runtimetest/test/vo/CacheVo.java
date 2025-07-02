package com.zj.runtimetest.test.vo;

import com.zj.runtimetest.vo.RequestInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author : jie.zhou
 * @date : 2025/6/30
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class CacheVo extends RequestInfo implements Serializable {

    private Long pid;

}
