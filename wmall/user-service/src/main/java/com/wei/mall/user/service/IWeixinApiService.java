package com.wei.mall.user.service;

import com.wei.mall.user.domain.req.WeixinQrCodeReq;
import com.wei.mall.user.domain.res.WeixinQrCodeRes;
import com.wei.mall.user.domain.res.WeixinTokenRes;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IWeixinApiService {

    @GET("cgi-bin/token")
    Call<WeixinTokenRes> getToken(
            @Query("grant_type") String grantType,
            @Query("appid") String appId,
            @Query("secret") String appSecret);

    @POST("cgi-bin/qrcode/create")
    Call<WeixinQrCodeRes> createQrCode(
            @Query("access_token") String accessToken,
            @Body WeixinQrCodeReq req);


}