package com.wei.mall.user.weixin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/*
* MessageTextEntity 是微信公众号消息的 Java 映射类。你看到的注解不是 Spring 的，
* 而是 XStream 库里的 @XStreamAlias，用来把 Java 对象和微信发来的 XML 互相转换。
* */

@Data
@XStreamAlias("xml")
public class MessageTextEntity {

    @XStreamAlias("ToUserName")
    private String toUserName;

    @XStreamAlias("FromUserName")
    private String fromUserName;

    @XStreamAlias("CreateTime")
    private String createTime;

    @XStreamAlias("MsgType")
    private String msgType;

    @XStreamAlias("Event")
    private String event;

    @XStreamAlias("EventKey")
    private String eventKey;

    @XStreamAlias("MsgId")
    private String msgId;

    @XStreamAlias("MsgID")
    private String msgID;

    @XStreamAlias("Status")
    private String status;

    @XStreamAlias("Ticket")
    private String ticket;

    @XStreamAlias("Content")
    private String content;


}