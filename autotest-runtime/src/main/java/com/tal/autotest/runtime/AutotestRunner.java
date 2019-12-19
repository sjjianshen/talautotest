package com.tal.autotest.runtime;

import com.tal.autotest.runtime.instrument.InstrumentAgentLoader;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class AutotestRunner extends BlockJUnit4ClassRunner {
    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public AutotestRunner(Class<?> klass) throws InitializationError {
        super(wrapClass(klass));
    }

    private static Class<?> wrapClass(Class<?> klass) {
        InstrumentAgentLoader.initialize();
        return klass;
    }
}
