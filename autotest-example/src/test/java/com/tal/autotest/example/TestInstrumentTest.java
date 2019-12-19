package com.tal.autotest.example;

import com.tal.autotest.example.util.ExampleUtil;
import com.tal.autotest.runtime.AutotestRunner;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static com.tal.autotest.runtime.mock.MockFrameWork.*;

@RunWith(AutotestRunner.class)
public class TestInstrumentTest {
    @Test
    public void test() {
        when(mock(ExampleUtil.class).multi(123, 1)).thenReturn(888);
        TestInstrument var2 = new TestInstrument();
        int i = var2.testInstrument(123, 1);
        assertThat(i, Is.is(123));
    }
}