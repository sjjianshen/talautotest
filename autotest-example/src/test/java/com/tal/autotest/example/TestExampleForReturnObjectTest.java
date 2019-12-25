package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleForReturnObjectTest {
   @Test
   public void case3() {
      TestExampleForReturnObject var1 = new TestExampleForReturnObject();
      User var2 = new User();
      var2.setName("sj");
      var2.setAge(30);
      Address var3 = new Address();
      var3.setCountry("CN");
      var3.setProvince("beijing");
      var2.setAddress(var3);
      Address var4 = var1.test(var2);
      MatcherAssert.assertThat(var4.getCountry(), Is.is("CN"));
      MatcherAssert.assertThat(var4.getProvince(), Is.is("beijing"));
   }

   @Test
   public void case4() {
      User var1 = new User();
      var1.setName("sj");
      var1.setAge(30);
      Address var2 = TestExampleForReturnObject.staticTest(var1);
      MatcherAssert.assertThat(var2, Is.is((Object)null));
   }
}
