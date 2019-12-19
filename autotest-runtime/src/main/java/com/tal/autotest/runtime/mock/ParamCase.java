package com.tal.autotest.runtime.mock;

import java.util.List;

public class ParamCase implements IMockCase {
    public Object ret;
    public List<ITerm> paramTerms;

    public ParamCase(List<ITerm> paramTerms, Object ret) {
        this.paramTerms = paramTerms;
        this.ret = ret;
    }

    @Override
    public boolean match(List<Object> params) {
        int size = paramTerms.size();
        if (size != params.size()) {
            return false;
        }

        for (int i = 0; i< size; i++) {
            if (!paramTerms.get(i).match(params.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getValue() {
        return ret;
    }
}
