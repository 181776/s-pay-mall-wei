package com.wei.mall.user.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "weixin.config")
public class WeixinProperties {
  private String originalId;
  private String token ;
  private String appId ;
  private String appSecret;


}
