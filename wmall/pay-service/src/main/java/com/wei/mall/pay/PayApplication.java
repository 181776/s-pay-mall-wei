package com.wei.mall.pay;


import com.wei.mall.api.config.DefaultFeignConfig;
import com.wei.mall.pay.config.AliPayConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = "com.wei.mall.api.client", defaultConfiguration = DefaultFeignConfig.class)
@EnableConfigurationProperties(AliPayConfig.class)
@MapperScan("com.wei.mall.pay.mapper")
@EnableScheduling
@SpringBootApplication
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}
