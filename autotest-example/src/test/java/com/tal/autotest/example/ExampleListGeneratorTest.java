package com.tal.autotest.example;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class ExampleListGeneratorTest {
   @Test
   public void case4() {
      ExampleListGenerator var1 = new ExampleListGenerator();
      String[] var2 = var1.generateA(10);
      MatcherAssert.assertThat(var2.length, Is.is(Integer.valueOf(11)));
   }

   @Test
   public void case3() {
      ExampleListGenerator var1 = new ExampleListGenerator();
      List var2 = var1.generate(2);
      MatcherAssert.assertThat(var2.size(), Is.is(Integer.valueOf(3)));
   }
}
