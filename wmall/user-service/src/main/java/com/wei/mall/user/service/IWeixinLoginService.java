package com.wei.mall.user.service;


public interface IWeixinLoginService {

    String createQrcodeTicket();

    String checkQrcodeTicket(String ticket);

    void saveLoginState(String ticket, String openid);

}
