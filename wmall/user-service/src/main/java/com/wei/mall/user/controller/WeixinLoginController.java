package com.wei.mall.user.controller;


import cn.hutool.core.util.StrUtil;
import com.wei.mall.user.domain.vo.UserLoginVO;
import com.wei.mall.user.service.IUserService;
import com.wei.mall.user.service.IWeixinLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class WeixinLoginController {

    private final IWeixinLoginService weixinLoginService;
    private final IUserService userService;

    @GetMapping("/weixin/qrcode-ticket")
    public String createQrcodeTicket() {
        String qrcodeTicket = weixinLoginService.createQrcodeTicket();
        return qrcodeTicket;
    }


    @GetMapping("/weixin/check-login")
    public UserLoginVO checkLogin(@RequestParam String ticket) {
        String openid = weixinLoginService.checkQrcodeTicket(ticket);
        if (StrUtil.isBlank(openid)) {
            return null; // 还没扫码，前端继续轮询
        }
        return userService.loginByOpenid(openid);
    }


}