package com.tal.autotest.runtime.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class InstrumentAgent {
    public static Instrumentation instrumentation;
    private static ClassFileTransformer transformer;

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
}
