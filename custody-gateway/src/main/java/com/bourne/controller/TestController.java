package com.bourne.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class TestController {

    @Value("${user.value}")
    @Getter
    @Setter
    private String value;

    @Value("${user.desc}")
    @Getter
    @Setter
    private String desc;

    @GetMapping("/test")
    private String test() {
        return getValue() + " " + getDesc();

    }
}
