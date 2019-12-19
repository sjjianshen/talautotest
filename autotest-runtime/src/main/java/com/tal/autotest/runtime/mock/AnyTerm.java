package com.tal.autotest.runtime.mock;

public class AnyTerm implements ITerm {
    @Override
    public boolean match(Object param) {
        return true;
    }
}
