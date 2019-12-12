package com.tal.autotest.example;

import java.util.ArrayList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class ListTestExampleTest {
   @Test
   public void case2() {
      ListTestExample var2 = new ListTestExample();
      ArrayList var3 = new ArrayList();
      Address var4 = new Address();
      var4.setCountry("HB");
      var3.add(var4);
      Address var5 = new Address();
      var5.setCountry("HN");
      var3.add(var5);
      int var6 = var2.addrList(var3);
      MatcherAssert.assertThat(var6, Is.is(2));
   }
}
