//package com.zj.runtimetest;
//
//import net.bytebuddy.agent.builder.AgentBuilder;
//import net.bytebuddy.asm.Advice;
//import net.bytebuddy.description.type.TypeDescription;
//import net.bytebuddy.dynamic.DynamicType;
//import net.bytebuddy.matcher.ElementMatchers;
//import net.bytebuddy.utility.JavaModule;
//import org.springframework.context.event.ContextRefreshedEvent;
//
//import java.security.ProtectionDomain;
//
//public class MyTransformer implements AgentBuilder.Transformer {
//    @Override
//    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
//                                            TypeDescription typeDescription,
//                                            ClassLoader classLoader,
//                                            JavaModule module,
//                                            ProtectionDomain protectionDomain) {
//        return transform(builder, typeDescription, classLoader, module);
//    }
//
//    /**
//     * 低版本没有protectionDomain参数，兼容一下
//     * @param builder
//     * @param typeDescription
//     * @param classLoader
//     * @param module
//     * @return
//     */
//    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
//                                            TypeDescription typeDescription,
//                                            ClassLoader classLoader,
//                                            JavaModule module) {
//        return builder.visit(Advice.to(RuntimeTestAttach.SpringAdvice.class)
//                .on(ElementMatchers.named("onApplicationEvent")
//                        .and(ElementMatchers.takesArgument(0, ContextRefreshedEvent.class))));
//    }
//}
