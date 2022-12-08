package com.bourne.controller;

import com.bourne.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群
 * @Version 1.0
 **/

@RestController
@RequestMapping("/api/product/v1")
public class ProductController {


    /**
     * 查看商品详情
     * @param productId
     * @return
     */
    @GetMapping("/detail/{product_id}")
    public Result detail(@PathVariable("product_id") long productId){

        return Result.info("1","2","3");
    }
}
