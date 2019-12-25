package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleTest {
   @Test
   public void case1() {
      TestExample var1 = new TestExample();
      Boolean var10001 = Boolean.valueOf("true");
      User var2 = new User();
      var2.setName("sj");
      var2.setAge(30);
      Address var3 = new Address();
      var3.setCountry("CN");
      var3.setProvince("beijing");
      var2.setAddress(var3);
      String var4 = var1.test((boolean)var10001, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, var2);
      MatcherAssert.assertThat(var4, Is.is("1"));
   }

   @Test
   public void case2() {
      Boolean var10000 = Boolean.valueOf("true");
      User var1 = new User();
      var1.setName("sj");
      var1.setAge(30);
      String var2 = TestExample.staticTest((boolean)var10000, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, var1);
      MatcherAssert.assertThat(var2, Is.is("1"));
   }
}
