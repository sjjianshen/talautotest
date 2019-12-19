package com.tal.autotest.runtime.mock;

import java.util.List;

public class DefaultCase implements IMockCase {
    public Object ret;

    public DefaultCase(Object ret) {
        this.ret = ret;
    }

    @Override
    public boolean match(List<Object> params) {
        return true;
    }

    @Override
    public Object getValue() {
        return ret;
    }
}
