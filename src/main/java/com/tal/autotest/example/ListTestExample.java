package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

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
}
