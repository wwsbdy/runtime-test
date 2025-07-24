//package com.zj.runtimetest.exp;
//
//import javax.tools.SimpleJavaFileObject;
//import java.net.URI;
//
///**
// * javax.tools在jdk9以上报ClassNotFoundException，所以用ecj
// * @author : jie.zhou
// * @date : 2025/7/22
// */
//public class JavaSourceFromString extends SimpleJavaFileObject {
//    final String code;
//
//    public JavaSourceFromString(String name, String code) {
//        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
//        this.code = code;
//    }
//
//    @Override
//    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
//        return code;
//    }
//}