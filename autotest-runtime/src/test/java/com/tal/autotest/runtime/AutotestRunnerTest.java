package com.tal.autotest.runtime;

import com.tal.autotest.runtime.mock.MockFrameWork;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AutotestRunner.class)
public class AutotestRunnerTest {
    @Test
    public void testtest() {
        Rest test = MockFrameWork.mock(Rest.class);
        Result value = new Result(1.1);
        MockFrameWork.when(test.divide(1, 0)).thenReturn(value);
        assertThat(test.divide(1,0), is(value));
    }

    static class Rest {
        public Result divide(int i, int j) {
            return new Result(i / j);
        }
    }

    static class Result {
        public Result(double value) {
            this.value = value;
        }

        public double value;
    }
}