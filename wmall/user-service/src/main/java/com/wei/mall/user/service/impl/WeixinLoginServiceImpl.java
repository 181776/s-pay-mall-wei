package com.wei.mall.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.wei.mall.user.cache.LoginRedisCache;
import com.wei.mall.user.config.WeixinProperties;
import com.wei.mall.user.domain.req.WeixinQrCodeReq;
import com.wei.mall.user.domain.res.WeixinQrCodeRes;
import com.wei.mall.user.domain.res.WeixinTokenRes;
import com.wei.mall.user.service.IWeixinApiService;
import com.wei.mall.user.service.IWeixinLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import java.io.IOException;


@Service
@RequiredArgsConstructor
public class WeixinLoginServiceImpl implements IWeixinLoginService {


    private final WeixinProperties weixinProperties;
    private final IWeixinApiService weixinApiService;
    private final LoginRedisCache loginRedisCache;

    @Override
    public String createQrcodeTicket() {
        try {
            // ① 拿 access_token
            String accessToken=loginRedisCache.getWeixinAccessToken(weixinProperties.getAppId());
            if (StrUtil.isBlank(accessToken)){
                Call<WeixinTokenRes> tokenCall = weixinApiService.getToken(
                        "client_credential", weixinProperties.getAppId(), weixinProperties.getAppSecret());
                WeixinTokenRes tokenRes = tokenCall.execute().body();
                if (tokenRes == null || StrUtil.isBlank(tokenRes.getAccess_token())) {
                    throw new RuntimeException("获取 access_token 失败: " + tokenRes);
                }
                accessToken = tokenRes.getAccess_token();
                loginRedisCache.setWeixinAccessToken(weixinProperties.getAppId(),accessToken);
            }


            // ② 组装二维码请求体
            WeixinQrCodeReq req = WeixinQrCodeReq.builder()
                    .expire_seconds(2592000)  // 临时二维码最长 30 天
                    .action_name(WeixinQrCodeReq.ActionNameTypeVO.QR_SCENE.getCode())
                    .action_info(WeixinQrCodeReq.ActionInfo.builder()
                            .scene(WeixinQrCodeReq.ActionInfo.Scene.builder()
                                    .scene_id(100601)   // 场景值，固定一个即可
                                    .build())
                            .build())
                    .build();
            // ③ 调微信创建二维码
            Call<WeixinQrCodeRes> qrCall = weixinApiService.createQrCode(accessToken, req);
            WeixinQrCodeRes qrRes = qrCall.execute().body();
            if (qrRes == null || StrUtil.isBlank(qrRes.getTicket())) {
                throw new RuntimeException("创建二维码失败: " + qrRes);
            }
            return qrRes.getTicket();
        } catch (IOException e) {
            throw new RuntimeException("调用微信 API 失败", e);
        }

    }

    @Override
    public String checkQrcodeTicket(String ticket) {
        String openId = loginRedisCache.getLoginOpenid(ticket);
        return openId;
    }

    @Override
    public void saveLoginState(String ticket, String openid) {
        loginRedisCache.setLoginOpenid(ticket, openid);
    }


}
