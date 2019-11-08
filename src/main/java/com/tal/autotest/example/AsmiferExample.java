package com.tal.autotest.example;

import com.tal.autotest.example.controller.ExampleController;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ExampleApp.class}, initializers={ConfigFileApplicationContextInitializer.class})
public class AsmiferExample {
    @Autowired
    private ExampleController var2;

    @Test
    public void testAdd() {
        Integer var3 = var2.add(1111, 3333);
        MatcherAssert.assertThat(var3, Is.<Integer>is(4444));

    }
}
