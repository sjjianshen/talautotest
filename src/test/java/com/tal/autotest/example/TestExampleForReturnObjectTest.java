package com.tal.autotest.example;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class TestExampleForReturnObjectTest {
   @Test
   public void case4() {
      User var1 = new User();
      var1.setName("sj");
      Address var2 = TestExampleForReturnObject.staticTest(var1);
      MatcherAssert.assertThat(var2.getCountry(), Is.is("CN"));
   }
}
