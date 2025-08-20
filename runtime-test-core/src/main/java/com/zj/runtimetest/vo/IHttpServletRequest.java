package com.zj.runtimetest.vo;

/**
 * http请求接口
 * @author 19242
 */
public interface IHttpServletRequest {
    void setAttribute(String name, Object value);

    void addHeader(String name, Object value);
}
