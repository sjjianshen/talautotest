package com.tal.autotest.runtime.mock;

public class RealTerm implements ITerm {
    public Object real;

    public RealTerm(Object param) {
        this.real = param;
    }

    @Override
    public boolean match(Object param) {
        return real.equals(param);
    }
}
