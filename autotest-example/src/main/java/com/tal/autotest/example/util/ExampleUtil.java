package com.tal.autotest.example.util;

import org.springframework.stereotype.Component;

@Component
public class ExampleUtil {
    public int multi(int p1, int p2) {
        return p1 * p2;
    }

    public String add(String s1, int i) {
        return s1 + String.valueOf(i);
    }
}
