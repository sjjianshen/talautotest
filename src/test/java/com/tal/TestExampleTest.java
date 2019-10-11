package com.tal;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleTest {
   @Test
   public void case999() {
      TestExample var2 = new TestExample();
      User var3 = new User();
      var3.setName("sj");
      String var4 = var2.test(true, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, var3);
      MatcherAssert.assertThat(var4, Is.is("1"));
   }

   @Test
   public void case998() {
      User var1 = new User();
      var1.setName("sj");
      String var2 = TestExample.staticTest(true, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, var1);
      MatcherAssert.assertThat(var2, Is.is("1"));
   }
}
