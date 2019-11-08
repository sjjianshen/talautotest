package com.tal.autotest.example;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class ExampleListGeneratorTest {
   @Test
   public void case4() {
      ExampleListGenerator var2 = new ExampleListGenerator();
      String[] var3 = var2.generateA((int)Integer.valueOf(10));
      MatcherAssert.assertThat(var3.length, Is.is(Integer.valueOf(11)));
   }

   @Test
   public void case3() {
      ExampleListGenerator var2 = new ExampleListGenerator();
      List var3 = var2.generate((int)Integer.valueOf(2));
      MatcherAssert.assertThat(var3.size(), Is.is(Integer.valueOf(3)));
   }
}
