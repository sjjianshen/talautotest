package com.tal.autotest.runtime.instrument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        try {
            cr.accept(methodAdapter, Opcodes.ASM5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        classesInstrumented.add(className);
        byte[] bytes = cw.toByteArray();
        if (className.contains("TestInstrument")) {
            try {
                Files.write(Paths.get("/Users/jianshen/workspace/autotest/TestInstrument.class"), bytes, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }
}
