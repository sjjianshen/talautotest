package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        List<String> list = new ArrayList<>();
        boolean var1 = (boolean) RoundUtils.isEnglish("不是英语");
        MatcherAssert.assertThat(Boolean.valueOf(var1), Is.is(false));
        return "0";
    }

    public String not(boolean b) {
        if (b) {
            return "false";
        }
        return "true";
    }

    public void testNot() {
        TestExample testExample = new TestExample();
        boolean b = Boolean.valueOf("true");
        char c = "1".charAt(0);
        short s = Short.valueOf("1");
        int i = Integer.valueOf("1");
        long l = Long.valueOf("1");
        float f = Float.valueOf("1");
        double d = Double.valueOf("1");
        byte bt = Byte.valueOf("1");
        User u = new User();
        u.setName("sj");
        u.setAge(30);
        Address a = new Address();
        a.setCountry("BJ");
        a.setCountry("CN");
        u.setAddress(a);
        String res = testExample.test(b, c,s,i
                , l, f, d, bt
                , u
        );
    }
}
