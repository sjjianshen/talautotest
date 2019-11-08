package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleTest {
   @Test
   public void case1() {
      TestExample var2 = new TestExample();
      Boolean var10001 = (Boolean)true;
      Short var10003 = (Short)true;
      Integer var10004 = Integer.valueOf(3);
      Long var10005 = (Long)4L;
      Float var10006 = (Float)5.0F;
      Double var10007 = (Double)6.0D;
      Char var10008 = (Char)true;
      User var3 = new User();
      var3.setName("sj");
      String var4 = var2.test((boolean)var10001, '1', (short)var10003, (int)var10004, (long)var10005, (float)var10006, (double)var10007, (byte)var10008, var3);
      MatcherAssert.assertThat(var4, Is.is("1"));
   }

   @Test
   public void case2() {
      // $FF: Couldn't be decompiled
   }
}
