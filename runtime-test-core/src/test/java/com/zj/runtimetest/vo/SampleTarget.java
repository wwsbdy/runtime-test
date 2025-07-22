package com.zj.runtimetest.vo;

/**
 * @author : jie.zhou
 * @date : 2025/7/22
 */
public class SampleTarget {
    public void target(String param) {
        System.out.println("original logic: " + param);
    }

    public static void main(String[] args) throws Exception {
//        SampleTarget st = new SampleTarget();
//        st.target("before-agent");
//
//        // 动态 attach agent
//        com.sun.tools.attach.VirtualMachine vm =
//                com.sun.tools.attach.VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));
//        vm.loadAgent("agent.jar"); // 填你的 agent jar 路径
//        Thread.sleep(1000);
//
//        st.target("after-agent");
    }

}
