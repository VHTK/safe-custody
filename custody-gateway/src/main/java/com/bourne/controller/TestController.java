package com.bourne.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Value("${user.value}")
    private String testStr;

    @GetMapping("/test")
    private String test(){
        return testStr;
    }
}
