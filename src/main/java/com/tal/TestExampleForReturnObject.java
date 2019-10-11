package com.tal;

import org.junit.Test;

public class TestExampleForReturnObject {
    public Address test(User p8) {
        return p8.getAddress();
    }

    @Test
    public static Address staticTest(User p8) {
        return p8.getAddress();
    }
}
