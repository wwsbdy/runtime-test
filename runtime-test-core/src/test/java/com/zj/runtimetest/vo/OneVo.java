package com.zj.runtimetest.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : jie.zhou
 * @date : 2025/7/4
 */
@Data
public class OneVo {

    public String test() {
        System.err.println("OneVo.test");
//        AgentUtil.end = true;
        return "OneVo.test";
    }

    public void testList(List<Person> list) {
        for (Person person : list) {
            System.out.println(person);
        }
//        AgentUtil.end = true;
    }


}
