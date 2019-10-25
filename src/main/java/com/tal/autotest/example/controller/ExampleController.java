package com.tal.autotest.example.controller;

import com.tal.autotest.example.service.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {
    @Autowired
    private ExampleService service;

    public int add(int input1, int input2) {
        return service.add(input1, input2);
    }
}
