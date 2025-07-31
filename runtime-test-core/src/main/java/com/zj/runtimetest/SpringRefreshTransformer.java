package com.zj.runtimetest;

import com.zj.runtimetest.utils.LogUtil;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * @author 19242
 */
public class SpringRefreshTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        if (!"org/springframework/context/support/AbstractApplicationContext".equals(className)) {
            return null;
        }
        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);

            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
//                    LogUtil.alwaysLog("[Agent] visitMethod: " + name + " " + descriptor);
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                    if ("refresh".equals(name) && "()V".equals(descriptor)) {
                        LogUtil.alwaysLog("[Agent] Found method: refresh");
                        return new MethodVisitor(Opcodes.ASM9, mv) {
                            @Override
                            public void visitInsn(int opcode) {
                                if (opcode == Opcodes.RETURN) {
                                    // 插入 AgentContextHolder.setContext(this);
                                    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                            "com/zj/runtimetest/AgentContextHolder",
                                            "setContext",
                                            "(Ljava/lang/Object;)V",
                                            false);
                                }
                                super.visitInsn(opcode);
                            }
                        };
                    }

                    return mv;
                }
            };
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();

        } catch (Exception e) {
            LogUtil.alwaysErr("[Agent] Error: " + e.getMessage());
            return null;
        }
    }
}