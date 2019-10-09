package com.tal;

import org.junit.Test;

public class TestExample {
    public String test(boolean p0, char p1, short p2, int p3, long p4, float p5, double p6, byte p7, User p8) {
        if (p0 && '1' == p1 && p2 == 2 && p3 == 3 && p4 == 4 && p5 == 5 && p6 == 6 && p7 ==7) {
            return "1";
        }

        if ("sj".equals(p8.getName()) && p8.getAge() == 30) {
            return "2";
        }

        return "0";
    }

    @Test
    public static String staticTest(boolean p0, char p1, short p2, int p3, long p4, float p5, double p6, byte p7, User p8) {
        if (p0 && '1' == p1 && p2 == 2 && p3 == 3 && p4 == 4 && p5 == 5 && p6 == 6 && p7 ==7) {
            return "1";
        }

        if ("sj".equals(p8.getName()) && p8.getAge() == 30) {
            return "2";
        }

        return "0";
    }
}
