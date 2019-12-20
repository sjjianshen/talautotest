package com.tal.autotest.runtime.instrument;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class MockMethodAdapterVisitor extends ClassVisitor {
    private Set<String> excludePrefix;
    private Set<String> excludeContains;
    private String className;

    {
        excludePrefix = new HashSet<>();
        excludePrefix.add("java/");
        excludePrefix.add("org/junit");
        excludePrefix.add("sun/");
        excludePrefix.add("com/sun");
        excludePrefix.add("com/intellij");
        excludePrefix.add("org/hamcrest");
        excludePrefix.add("org/jetbrains");
        excludePrefix.add("net/sf");
        excludePrefix.add("com/tal/autotest/runtime");

        excludeContains = new HashSet<>();
        excludeContains.add("EnhancerByCGLIB");
    }

    public MockMethodAdapterVisitor(int flags, ClassWriter cw, String className) {
        super(flags, cw);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (!shouldExclude(className)) {
            mv = new MockMethodVisitor(mv, access, name, desc);
        }
        return mv;
    }



    class MockMethodVisitor extends GeneratorAdapter {
        public MockMethodVisitor(MethodVisitor methodVisitor, int access, String methodName, String desc) {
            super(Opcodes.ASM5, methodVisitor, access, methodName, desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == INVOKEDYNAMIC || MockMethodAdapterVisitor.this.shouldExclude(owner)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }

            Type returnType = Type.getReturnType(desc);
            if (returnType.equals(Type.VOID_TYPE)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }

            Type[] args = Type.getArgumentTypes(desc);
            Map<Integer, Integer> locationMap = new HashMap<>();
            boolean popCallee = opcode != INVOKESTATIC;

            for (int index = args.length - 1; index >= 0; index--) {
                int loc = newLocal(args[index]);
                storeLocal(loc, args[index]);
                locationMap.put(index, loc);
            }

            int calleeLoc = -1;
            if (popCallee) {
                calleeLoc = newLocal(Type.getType(Object.class));
                storeLocal(calleeLoc);
            }

            push(args.length);
            newArray(Type.getType(Object.class));
            int arrloc = newLocal(Type.getType(Object[].class));
            storeLocal(arrloc);
            for (int index = 0; index < args.length; index++) {
                loadLocal(arrloc);
                push(index);
                int loc = locationMap.get(index);
                loadLocal(loc, args[index]);
                if (isPrimitiveType(args[index])) {
                    box(args[index]);
                }
                arrayStore(Type.getType(Object.class));
            }
            int retLoc = newLocal(returnType);

            visitLdcInsn(owner);
            visitLdcInsn(name + desc);
            loadLocal(arrloc);
            mv.visitMethodInsn(INVOKESTATIC,
                    "com/tal/autotest/runtime/mock/MockFrameWork",
                    "hasRegisteredMock", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Z",
                    false);

            Label l0 = newLabel();
            Label l1 = newLabel();
            visitJumpInsn(IFNE, l0);
            if (popCallee) {
                loadLocal(calleeLoc);
            }
            for (int index = 0; index < args.length; index++) {
                int loc = locationMap.get(index);
                loadLocal(loc, args[index]);
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            storeLocal(retLoc);
            visitJumpInsn(GOTO, l1);

            visitLabel(l0);
            visitLdcInsn(owner);
            visitLdcInsn(name + desc);
            loadLocal(arrloc);
            mv.visitMethodInsn(INVOKESTATIC,
                    "com/tal/autotest/runtime/mock/MockFrameWork",
                    "getRegisteredMock", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                    false);
            if (isPrimitiveType(returnType)) {
                unbox(returnType);
            } else {
                checkCast(returnType);
            }
            storeLocal(retLoc);
            visitLabel(l1);
            loadLocal(retLoc);
        }

        private boolean isPrimitiveType(Type arg) {
            return arg == Type.BOOLEAN_TYPE  ||
                    arg == Type.CHAR_TYPE ||
                    arg == Type.BYTE_TYPE ||
                    arg == Type.SHORT_TYPE ||
                    arg == Type.INT_TYPE ||
                    arg == Type.FLOAT_TYPE ||
                    arg == Type.LONG_TYPE ||
                    arg == Type.DOUBLE_TYPE;
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // The instrumentation adds a boolean to the stack at one point
            // which _may_ increase the max stack size. A ASM
            // doesn't manage to calculate the maximum stack size
            // correctly we just add one here
            super.visitMaxs(maxStack, maxLocals);
        }
    }

    private boolean shouldExclude(String owner) {
        for (String prefix : excludePrefix) {
            if (owner.startsWith(prefix)) {
                return true;
            }
        }

        for (String piece : excludeContains) {
            if (owner.contains(piece)) {
                return true;
            }
        }
        return false;
    }
}
