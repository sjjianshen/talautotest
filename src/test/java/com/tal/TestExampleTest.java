package com.tal;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleTest {
   @Test
   public void case999() {
      TestExample var1 = new TestExample();
      String var2 = var1.test(true, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var2, Is.is("1"));
   }

   @Test
   public void case0() {
      TestExample var1 = new TestExample();
      String var2 = var1.test(false, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var2, Is.is("-1"));
   }

   @Test
   public void case1() {
      TestExample var1 = new TestExample();
      String var2 = var1.test(true, '3', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var2, Is.is("-1"));
   }

   @Test
   public void case2() {
      TestExample var1 = new TestExample();
      String var2 = var1.test(true, '1', (short)234, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var2, Is.is("-1"));
   }

   @Test
   public void caseaaa() {
      String var1 = TestExample.staticTest(true, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var1, Is.is("1"));
   }

   @Test
   public void casea() {
      String var1 = TestExample.staticTest(false, '1', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var1, Is.is("-11"));
   }

   @Test
   public void caseb() {
      String var1 = TestExample.staticTest(true, '3', (short)2, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var1, Is.is("-11"));
   }

   @Test
   public void casec() {
      String var1 = TestExample.staticTest(true, '1', (short)234, 3, 4L, 5.0F, 6.0D, (byte)7, "8");
      MatcherAssert.assertThat(var1, Is.is("-11"));
   }
}
