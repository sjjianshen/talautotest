package com.tal.autotest.example;

import com.tal.autotest.example.util.ExampleUtil;

public class TestInstrument {
    public int testInstrument(int input1, int input2) {
        ExampleUtil exampleUtil = new ExampleUtil();
        return exampleUtil.multi(input1, input2);
    }
}
