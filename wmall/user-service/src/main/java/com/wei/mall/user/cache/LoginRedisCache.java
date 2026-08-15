package com.wei.mall.user.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LoginRedisCache {

    private static final String WEIXIN_ACCESS_TOKEN_KEY = "weixin:access_token:";
    private static final String LOGIN_TICKET_KEY = "login:ticket:";
    /** 微信 access_token 有效期 7200 秒，略提前过期 */
    private static final long WEIXIN_TOKEN_TTL_SECONDS = 7000L;
    /** 扫码 ticket 与 openid 映射，1 小时 */
    private static final long LOGIN_TICKET_TTL_SECONDS = 3600L;

    private final StringRedisTemplate stringRedisTemplate;

    public String getWeixinAccessToken(String appId) {
        return stringRedisTemplate.opsForValue().get(WEIXIN_ACCESS_TOKEN_KEY + appId);
    }

    public void setWeixinAccessToken(String appId, String accessToken) {
        stringRedisTemplate.opsForValue().set(
                WEIXIN_ACCESS_TOKEN_KEY + appId,
                accessToken,
                WEIXIN_TOKEN_TTL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    public String getLoginOpenid(String ticket) {
        return stringRedisTemplate.opsForValue().get(LOGIN_TICKET_KEY + normalizeTicket(ticket));
    }

    public void setLoginOpenid(String ticket, String openid) {
        stringRedisTemplate.opsForValue().set(
                LOGIN_TICKET_KEY + normalizeTicket(ticket),
                openid,
                LOGIN_TICKET_TTL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private String normalizeTicket(String ticket) {
        if (ticket == null) {
            return null;
        }
        return ticket.trim().replace(" ", "+");
    }
}
