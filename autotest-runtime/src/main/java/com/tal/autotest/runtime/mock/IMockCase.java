package com.tal.autotest.runtime.mock;

import java.util.List;

public interface IMockCase {
    boolean match(List<Object> params);
    Object getValue();
}
