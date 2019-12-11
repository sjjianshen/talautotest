package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class ListTestExampleTest {
   @Test
   public void case2() {
      ListTestExample var2 = new ListTestExample();
      int[] var3 = new int[]{1, 2, 11, 12};
      int[] var4 = var2.testArrayFilter(var3);
      MatcherAssert.assertThat(var4.length, Is.is(Integer.valueOf(2)));
   }
}
