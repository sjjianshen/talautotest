package com.tal.autotest.runtime.instrument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class AutotestClassTransformer implements ClassFileTransformer {
    private Set<String> classesInstrumented = new HashSet<>();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classesInstrumented.contains(className)) {
            return classfileBuffer;
        }
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MockMethodAdapterVisitor methodAdapter = new MockMethodAdapterVisitor(Opcodes.ASM5, cw, className);
        cr.accept(methodAdapter, Opcodes.ASM5);
        classesInstrumented.add(className);
        return cw.toByteArray();
    }
}
