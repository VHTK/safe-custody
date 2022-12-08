package com.bourne;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.bourne.mapper")
@EnableTransactionManagement
@EnableFeignClients
@EnableDiscoveryClient
public class ChainbaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChainbaseApplication.class, args);
    }
}
