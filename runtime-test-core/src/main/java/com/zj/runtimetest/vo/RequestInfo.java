/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zj.runtimetest.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 请求信息
 *
 * @author 19242
 */
@Data
public class RequestInfo implements Serializable {

    private static final long serialVersionUID = 4675988165854842908L;
    /**
     * 全链路名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 入参
     */
    private String requestJson;

    /**
     * 项目路径
     */
    private String projectBasePath;


    /**
     * 参数类型
     */
    private List<MethodParamInfo> parameterTypeList;

    private boolean staticMethod;

}
