package com.tal.autotest.example;

import com.tal.autotest.example.controller.ExampleController;
import com.tal.autotest.runtime.mock.AnyTerm;
import com.tal.autotest.runtime.mock.MockFrameWork;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ExampleApp.class}, initializers={ConfigFileApplicationContextInitializer.class})
public class AsmiferExample {
    @Autowired
    private ExampleController var2;

    @Test
    public void testAdd() {
        Integer var3 = var2.add(1111, 3333);
        MatcherAssert.assertThat(var3, Is.<Integer>is(4444));

    }

    public Object mock() {
        AnyTerm anyTerm = new AnyTerm();
//        Object res = MockFrameWork.getRegisterMock("13", "123", new String[] {"123"});
//        if (res == null) {
//            res = anyTerm.match("123");
//        }

        boolean res = anyTerm.match("123");
        String resStr = "";
        if (res) {
            resStr = "1";
        } else {
            resStr = "2";
        }
        return resStr;
    }
}
