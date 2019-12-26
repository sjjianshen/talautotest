package com.tal.autotest.runtime.instrument;

import com.tal.autotest.runtime.mock.MockFrameWork;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

public class AutotestClassTransformer implements ClassFileTransformer {
    private Set<String> classesInstrumented = new HashSet<>();
    private boolean active = true;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (
                !active||
//                classesInstrumented.contains(className) ||
                className.contains("com/tal/autotest")  ||
                className.contains("$")
        ) {
            return classfileBuffer;
        }
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MockMethodAdapterVisitor methodAdapter = new MockMethodAdapterVisitor(Opcodes.ASM5, cw, className);
        try {
            cr.accept(methodAdapter, Opcodes.ASM5 | EXPAND_FRAMES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        classesInstrumented.add(className);
        return cw.toByteArray();
    }

    public void active() {
        active = true;
    }

    public void inActive() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
