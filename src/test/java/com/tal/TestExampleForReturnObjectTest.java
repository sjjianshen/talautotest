package com.tal;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleForReturnObjectTest {
   @Test
   public void case999() {
      TestExampleForReturnObject var2 = new TestExampleForReturnObject();
      User var3 = new User();
      var3.setName("sj");
      Address var4 = var2.test(var3);
      MatcherAssert.assertThat(var4.getCountry(), Is.is("CN"));
      MatcherAssert.assertThat(var4.getProvince(), Is.is("beijing"));
   }

   @Test
   public void case998() {
      User var1 = new User();
      var1.setName("sj");
      Address var2 = TestExampleForReturnObject.staticTest(var1);
   }
}
