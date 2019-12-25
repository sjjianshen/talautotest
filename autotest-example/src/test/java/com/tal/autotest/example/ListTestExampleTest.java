package com.tal.autotest.example;

import com.tal.autotest.example.util.ExampleUtil;
import com.tal.autotest.runtime.AutotestRunner;
import com.tal.autotest.runtime.mock.MockFrameWork;
import java.util.ArrayList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AutotestRunner.class)
public class ListTestExampleTest {
   @Test
   public void case2() {
      ExampleUtil var10000 = (ExampleUtil)MockFrameWork.mock(ExampleUtil.class);
      String var10001 = "123";
      MockFrameWork.when(1).thenReturn("xxx");
      ListTestExample var1 = new ListTestExample();
      ArrayList var2 = new ArrayList();
      Address var3 = new Address();
      var3.setCountry("HB");
      var2.add(var3);
      Address var4 = new Address();
      var4.setCountry("HN");
      var2.add(var4);
      int var5 = var1.addrList(var2);
      MatcherAssert.assertThat(var5, Is.is(2));
   }
}
