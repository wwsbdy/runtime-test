//package com.zj.runtimetest.exp;
//
//import javax.tools.SimpleJavaFileObject;
//import java.io.ByteArrayOutputStream;
//import java.io.OutputStream;
//import java.net.URI;
//
//
///**
// * javax.tools在jdk9以上报ClassNotFoundException，所以用ecj
// * @author : jie.zhou
// * @date : 2025/7/22
// */
//public class InMemoryClassJavaFileObject extends SimpleJavaFileObject {
//    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//    public InMemoryClassJavaFileObject(String className, Kind kind) {
//        super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
//    }
//
//    @Override
//    public OutputStream openOutputStream() {
//        return outputStream;
//    }
//
//    public byte[] getBytes() {
//        return outputStream.toByteArray();
//    }
//}