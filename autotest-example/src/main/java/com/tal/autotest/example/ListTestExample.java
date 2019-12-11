package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListTestExample {
    @Test
    public void verifyList() {
        ExampleListGenerator exampleListGenerator = new ExampleListGenerator();
        List<String> res = exampleListGenerator.generate(10);
        MatcherAssert.assertThat(res.size(), Is.<Integer>is(11));

        String[] resa = exampleListGenerator.generateA(10);
        MatcherAssert.assertThat(resa.length, Is.<Integer>is(11));
    }

    public List<Integer> testFilter(List<Integer> ids) {
        ids.removeIf(integer -> integer > 10);
        return ids;
    }

    public int[] testArrayFilter(int[] ids) {
        List<Integer> idList = new ArrayList<>();
        for (int i : ids) {
            if (i < 10) {
                idList.add(i);
            }
        }

        return new int[]{1,2};
    }

    public int[] arrayCompose() {
        int[] iarr = new int[50];
        iarr[0] = 1;
        iarr[1] = 1;
        iarr[2] = 1;
        iarr[3] = 1;
        iarr[4] = 1;

        return iarr;
    }
}
