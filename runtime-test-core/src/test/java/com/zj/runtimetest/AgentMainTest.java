package com.zj.runtimetest;

import com.zj.runtimetest.utils.AgentUtil;
import com.zj.runtimetest.utils.JsonUtil;
import com.zj.runtimetest.vo.MethodParamInfo;
import com.zj.runtimetest.vo.OneVo;
import com.zj.runtimetest.vo.Person;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : jie.zhou
 * @date : 2025/7/2
 */
public class AgentMainTest {

    @After
    public void after() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Test
    public void testLoadAgentAtRuntime() throws Exception {
        AgentUtil.run("com.zj.runtimetest.vo.OneVo",
                "test",
                Collections.emptyList(),
                null);
    }
    @Test
    public void testMethodArg() throws Exception {
        Method method = OneVo.class.getDeclaredMethod("testList", List.class);
        List<Person> personList = new ArrayList<>();
        personList.add(new Person("na", 18, null));
        HashMap<Object, Object> requestJson = new HashMap<>();
        requestJson.put("list", personList);
        // testList(List<Person> list)
        AgentUtil.run(method, JsonUtil.toJsonString(requestJson), new MethodParamInfo("list", "java.util.List"));
        personList.add(new Person("cjasidj", 1920, Stream.of("121", "123").collect(Collectors.toList())));
        AgentUtil.run(method, JsonUtil.toJsonString(requestJson), new MethodParamInfo("list", "java.util.List"));
    }
    @Test
    public void testClassNotFound() throws Exception {
        AgentUtil.run("com.zj.runtimetest.vo.OneVo11",
                "test",
                Collections.emptyList(),
                null);
        AgentUtil.run("com.zj.runtimetest.vo.OneVo11",
                "test",
                Collections.emptyList(),
                null);
    }
    @Test
    public void testMethodNotFound() throws Exception {
        AgentUtil.run("com.zj.runtimetest.vo.OneVo",
                "test11",
                Collections.emptyList(),
                null);
        AgentUtil.run("com.zj.runtimetest.vo.OneVo",
                "test11",
                Collections.emptyList(),
                null);
    }
}
