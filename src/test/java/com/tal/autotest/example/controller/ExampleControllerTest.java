package com.tal.autotest.example.controller;

import com.tal.autotest.example.ExampleApp;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(
   classes = {ExampleApp.class},
   initializers = {ConfigFileApplicationContextInitializer.class}
)
public class ExampleControllerTest {
   @Autowired
   private ExampleController instance;

   @Test
   public void testAdd() {
      Integer var1 = this.instance.add((int)Integer.valueOf(1), (int)Integer.valueOf(3));
      MatcherAssert.assertThat(var1, Is.is(Integer.valueOf(7)));
   }
}
