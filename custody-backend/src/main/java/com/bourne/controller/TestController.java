package com.bourne.controller;

import com.bourne.feign.ProductFeignService;
import com.bourne.result.Result;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${user.value}")
    @Getter
    @Setter
    private String testStr;

    @GetMapping("/get")
    public Integer testGet(@RequestParam Long id){
        Result result = productFeignService.detail(id);
        return 1;
    }

    @GetMapping("/test")
    public String test(){
        return getTestStr();
    }
}


