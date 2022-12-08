package com.bourne.controller;

import com.bourne.feign.ProductFeignService;
import com.bourne.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ProductFeignService productFeignService;

    @GetMapping("/get")
    public Integer testGet(@RequestParam Long id){
        Result result = productFeignService.detail(id);
        return 1;
    }
}


