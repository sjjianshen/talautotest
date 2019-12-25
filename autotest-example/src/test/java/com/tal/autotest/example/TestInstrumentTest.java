package com.tal.autotest.example;

import com.tal.autotest.example.util.ExampleUtil;
import com.tal.autotest.runtime.AutotestRunner;
import com.tal.autotest.runtime.mock.MockFrameWork;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AutotestRunner.class)
public class TestInstrumentTest {
   @Test
   public void case2() {
      ExampleUtil var1 = (ExampleUtil)MockFrameWork.mock(ExampleUtil.class);
      MockFrameWork.when(var1.add("123", 1)).thenReturn("xxx");
      TestInstrument var2 = new TestInstrument();
      String var3 = var2.testAdd("123", 1);
      MatcherAssert.assertThat(var3, Is.is("xxx"));
   }
}
