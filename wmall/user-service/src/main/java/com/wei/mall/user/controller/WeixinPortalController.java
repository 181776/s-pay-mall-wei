package com.wei.mall.user.controller;



import com.alibaba.nacos.common.utils.StringUtils;
import com.wei.mall.user.config.WeixinProperties;
import com.wei.mall.user.service.IWeixinLoginService;
import com.wei.mall.user.weixin.MessageTextEntity;
import com.wei.mall.user.weixin.SignatureUtil;
import com.wei.mall.user.weixin.XmlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/weixin/portal")
@RequiredArgsConstructor
public class WeixinPortalController {

    private final WeixinProperties weixinProperties;
    private final IWeixinLoginService weixinLoginService;

    @GetMapping(value = "receive", produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {//任意一个为空
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }
            boolean check = SignatureUtil.check(weixinProperties.getToken(), signature, timestamp, nonce);
            if (!check) {
                return null;
            }
            return echostr;
        } catch (Exception e) {
            return null;
        }
    }

    /*用户在微信里扫二维码或给公众号发消息时，微信服务器会 POST 到你这台服务器
     * */
    @PostMapping(value = "receive", produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,//xml正文
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,//哪个用户
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        try {
            if (StringUtils.isAnyBlank(signature, timestamp, nonce)) {
                return "";
            }
            boolean check = SignatureUtil.check(weixinProperties.getToken(), signature, timestamp, nonce);
            if (!check) {
                return "";
            }
            // 消息转换
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            if ("event".equals(message.getMsgType())) {
                String event = message.getEvent();
                if (("SCAN".equals(event) || "subscribe".equals(event))
                        && StringUtils.isNotBlank(message.getTicket())) {
                    weixinLoginService.saveLoginState(message.getTicket(), openid);
                    return buildMessageTextEntity(openid, "登录成功");
                }
            }
            return buildMessageTextEntity(openid, "你 好，" + message.getContent());
        } catch (Exception e) {

            return "";
        }
    }
    private String buildMessageTextEntity(String openid, String content) {
        MessageTextEntity res = new MessageTextEntity();
        // 公众号分配的ID
        res.setFromUserName(weixinProperties.getOriginalId());
        res.setToUserName(openid);
        res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
        res.setMsgType("text");
        res.setContent(content);
        return XmlUtil.beanToXml(res);
    }
}
