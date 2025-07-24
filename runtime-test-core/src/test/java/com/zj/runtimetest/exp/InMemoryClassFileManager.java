//package com.zj.runtimetest.exp;
//
//import javax.tools.FileObject;
//import javax.tools.ForwardingJavaFileManager;
//import javax.tools.JavaFileManager;
//import javax.tools.JavaFileObject;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * javax.tools在jdk9以上报ClassNotFoundException，所以用ecj
// * @author : jie.zhou
// * @date : 2025/7/22
// */
//public class InMemoryClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {
//    private final Map<String, InMemoryClassJavaFileObject> compiledClassMap = new HashMap<>();
//
//    public InMemoryClassFileManager(JavaFileManager fileManager) {
//        super(fileManager);
//    }
//
//    @Override
//    public JavaFileObject getJavaFileForOutput(
//            JavaFileManager.Location location,
//            String className,
//            JavaFileObject.Kind kind,
//            FileObject sibling
//    ) {
//        InMemoryClassJavaFileObject fileObject = new InMemoryClassJavaFileObject(className, kind);
//        compiledClassMap.put(className, fileObject);
//        return fileObject;
//    }
//
//    public byte[] getClassBytes(String className) {
//        InMemoryClassJavaFileObject fileObject = compiledClassMap.get(className);
//        return fileObject != null ? fileObject.getBytes() : null;
//    }
//}