package com.bourne.feign;

import com.bourne.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "custody-chainbase")
public interface ProductFeignService {


    /**
     * 获取流量包商品详情
     * @param productId
     * @return
     */
    @GetMapping("/api/product/v1/detail/{product_id}")
    Result detail(@PathVariable("product_id") long productId);

}
