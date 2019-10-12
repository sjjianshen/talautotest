package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class RoundUtilsTest {
   @Test
   public void shouldReturnTure() {
      Boolean var1 = (Boolean)RoundUtils.isEnglish("asd");
      MatcherAssert.assertThat(var1, Is.is((Boolean)true));
   }

   @Test
   public void shouldReturnFalse() {
      Boolean var1 = (Boolean)RoundUtils.isEnglish("不是英语");
      MatcherAssert.assertThat(var1, Is.is((Boolean)false));
   }
}
