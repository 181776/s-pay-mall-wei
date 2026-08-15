-- wmall-pay 支付库
CREATE DATABASE IF NOT EXISTS `wmall-pay` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `wmall-pay`;

DROP TABLE IF EXISTS `pay_order`;

CREATE TABLE `pay_order`
(
    id               BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    biz_order_no     BIGINT                                  NOT NULL COMMENT '业务订单号',
    pay_order_no     BIGINT        DEFAULT 0                 NOT NULL COMMENT '支付单号',
    biz_user_id      BIGINT                                  NOT NULL COMMENT '支付用户id',
    pay_channel_code VARCHAR(30)   DEFAULT '0'               NOT NULL COMMENT '支付渠道编码',
    amount           INT                                     NOT NULL COMMENT '支付金额，单位分',
    pay_type         TINYINT       DEFAULT 5                 NOT NULL COMMENT '支付类型，1：h5,2:小程序，3：公众号，4：扫码，5：余额支付',
    status           TINYINT       DEFAULT 0                 NOT NULL COMMENT '支付状态，0：待提交，1:待支付，2：支付超时或取消，3：支付成功',
    expand_json      VARCHAR(1024) DEFAULT ''                NOT NULL COMMENT '拓展字段，用于传递不同渠道单独处理的字段',
    result_code      VARCHAR(20)   DEFAULT ''                NULL COMMENT '第三方返回业务码',
    result_msg       VARCHAR(50)   DEFAULT ''                NULL COMMENT '第三方返回提示信息',
    pay_success_time DATETIME                                NULL COMMENT '支付成功时间',
    pay_over_time    DATETIME                                NOT NULL COMMENT '支付超时时间',
    qr_code_url      VARCHAR(255)                            NULL COMMENT '支付二维码链接',
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creater          BIGINT        DEFAULT 0                 NOT NULL COMMENT '创建人',
    updater          BIGINT        DEFAULT 0                 NOT NULL COMMENT '更新人',
    is_delete        BIT           DEFAULT b'0'              NOT NULL COMMENT '逻辑删除',
    CONSTRAINT biz_order_no UNIQUE (biz_order_no),
    CONSTRAINT pay_order_no UNIQUE (pay_order_no)
) COMMENT '支付订单' CHARSET = utf8mb4;
