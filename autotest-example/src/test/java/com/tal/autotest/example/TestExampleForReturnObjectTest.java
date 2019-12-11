package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleForReturnObjectTest {
   @Test
   public void case3() {
      TestExampleForReturnObject var2 = new TestExampleForReturnObject();
      User var3 = new User();
      var3.setName("sj");
      var3.setAge(30);
      Address var4 = new Address();
      var4.setCountry("CN");
      var4.setProvince("beijing");
      var3.setAddress(var4);
      Address var5 = var2.test(var3);
      MatcherAssert.assertThat(var5.getCountry(), Is.is("CN"));
      MatcherAssert.assertThat(var5.getProvince(), Is.is("beijing"));
   }

   @Test
   public void case4() {
      User var1 = new User();
      var1.setName("sj");
      var1.setAge(30);
      MatcherAssert.assertThat(TestExampleForReturnObject.staticTest(var1), Is.is((Object)null));
   }
}
