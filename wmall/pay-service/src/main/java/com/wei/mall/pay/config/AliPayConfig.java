package com.wei.mall.pay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {

    private String app_id;
    private String merchant_private_key;
    private String alipay_public_key;
    private String notify_url;
    private String return_url;
    private String gatewayUrl;
    private String sign_type = "RSA2";
    private String charset = "utf-8";
    private String format = "json";

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl, app_id, merchant_private_key,
                format, charset, alipay_public_key, sign_type);
    }
}