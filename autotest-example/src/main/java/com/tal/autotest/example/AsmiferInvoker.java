package com.tal.autotest.example;

import org.objectweb.asm.util.ASMifier;

public class AsmiferInvoker {
    public static void main(String[] args) throws Exception {
        ASMifier.main(new String[] {"com.tal.autotest.example.AsmiferExample"});
    }
}
