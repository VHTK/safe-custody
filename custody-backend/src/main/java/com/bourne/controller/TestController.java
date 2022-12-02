package com.bourne.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: vhtk
 * @Description:
 * @Date: 2020/6/22
 */
@RestController
public class TestController {

    @GetMapping("/get")
    public Integer testGet(@RequestParam Integer id){
        return id;
    }
}


