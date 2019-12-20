package com.tal.autotest.example;

import com.tal.autotest.example.util.ExampleUtil;

public class TestInstrument {
    public int testMulti(int input1, int input2) {
        ExampleUtil exampleUtil = new ExampleUtil();
        return exampleUtil.multi(input1, input2);
    }

    public String testAdd(String input1, int input2) {
        ExampleUtil exampleUtil = new ExampleUtil();
        return exampleUtil.add(input1, input2);
    }
}
