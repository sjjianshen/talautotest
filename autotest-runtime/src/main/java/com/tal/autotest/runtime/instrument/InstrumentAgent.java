package com.tal.autotest.runtime.instrument;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class InstrumentAgent {
    public static Instrumentation instrumentation;
    private static AutotestClassTransformer transformer;

    static {
        transformer = new AutotestClassTransformer();
    }

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        inst.addTransformer(transformer, false);
    }

    public static void agentmain(String args, Instrumentation inst) {
        instrumentation = inst;
        inst.addTransformer(transformer, false);
    }

    public static void reDefineClass(Class<?> clz, byte[] byteArray) {
        if (instrumentation == null) {
            return;
        }
        try {
            instrumentation.redefineClasses(new ClassDefinition(clz, byteArray));
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            e.printStackTrace();
        }
    }


    public static void active() {
        transformer.active();
    }

    public static void inActive() {
        transformer.inActive();
    }

    public static boolean isActive() {
        return transformer.isActive();
    }
}
