package com.wei.mall.user.domain.res;

import lombok.Data;

@Data
public class WeixinTokenRes {
    private String access_token;//接口调用凭证
    private Integer expires_in;//有效时间
    private String errcode;//错误码
    private String errmsg;


}
