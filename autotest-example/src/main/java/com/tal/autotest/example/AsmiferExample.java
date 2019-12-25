package com.tal.autotest.example;

import com.tal.autotest.example.util.ExampleUtil;
import org.hamcrest.core.Is;

import static com.tal.autotest.runtime.mock.MockFrameWork.mock;
import static com.tal.autotest.runtime.mock.MockFrameWork.when;
import static org.hamcrest.MatcherAssert.assertThat;

public class AsmiferExample {
    public void test() {
        when(mock(ExampleUtil.class).add("123", 1)).thenReturn("xxx");
        TestInstrument var2 = new TestInstrument();
        String i = var2.testAdd("123", 1);
        assertThat(i, Is.is("1231"));
    }
}
