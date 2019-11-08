package com.tal.autotest.example;

import java.util.ArrayList;
import java.util.List;

public class ExampleListGenerator {
    public List<String> generate(int limit) {
        List<String> res = new ArrayList<>();
        for (int i = 0; i <= limit; i++) {
            res.add(String.valueOf(i));
        }
        return res;
    }

    public String[] generateA(int limit) {
        List<String> res = new ArrayList<>();
        for (int i = 0; i <= limit; i++) {
            res.add(String.valueOf(i));
        }
        return res.toArray(new String[0]);
    }
}
