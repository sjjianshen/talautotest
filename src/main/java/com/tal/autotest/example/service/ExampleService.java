package com.tal.autotest.example.service;

import com.tal.autotest.example.util.ExampleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {
    @Autowired
    private ExampleUtil exampleUtil;
    public int add(int input1, int input2) {
        return input1 + input2 + exampleUtil.multi(input1, input2);
    }
}
