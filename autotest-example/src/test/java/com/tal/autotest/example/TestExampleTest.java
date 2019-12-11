package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleTest {
   @Test
   public void case1() {
      TestExample var2 = new TestExample();
      Boolean var10001 = Boolean.valueOf("true");
      User var3 = new User();
      var3.setName("sj");
      var3.setAge(30);
      Address var4 = new Address();
      var4.setCountry("CN");
      var4.setProvince("beijing");
      var3.setAddress(var4);
      String var5 = var2.test((boolean)var10001, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, var3);
      MatcherAssert.assertThat(var5, Is.is("1"));
   }

   @Test
   public void case2() {
      Boolean var10000 = Boolean.valueOf("true");
      User var1 = new User();
      var1.setName("sj");
      var1.setAge(30);
      MatcherAssert.assertThat(TestExample.staticTest((boolean)var10000, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, var1), Is.is("1"));
   }
}
