-- wmall-trade 订单库
CREATE DATABASE IF NOT EXISTS `wmall-trade` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `wmall-trade`;

DROP TABLE IF EXISTS `order_logistics`;
DROP TABLE IF EXISTS `order_detail`;
DROP TABLE IF EXISTS `order`;

CREATE TABLE `order`
(
    id           BIGINT                              NOT NULL COMMENT '订单id' PRIMARY KEY,
    total_fee    INT       DEFAULT 0                 NOT NULL COMMENT '总金额，单位为分',
    payment_type TINYINT(1) UNSIGNED ZEROFILL        NOT NULL COMMENT '支付类型，1、支付宝，2、微信，3、扣减余额',
    user_id      BIGINT                              NOT NULL COMMENT '用户id',
    status       TINYINT(1)                          NULL COMMENT '订单的状态，1、未付款 2、已付款,未发货 3、已发货,未确认 4、确认收货，交易成功 5、交易取消，订单关闭 6、交易结束，已评价',
    create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    pay_time     TIMESTAMP                           NULL COMMENT '支付时间',
    consign_time TIMESTAMP                           NULL COMMENT '发货时间',
    end_time     TIMESTAMP                           NULL COMMENT '交易完成时间',
    close_time   TIMESTAMP                           NULL COMMENT '交易关闭时间',
    comment_time TIMESTAMP                           NULL COMMENT '评价时间',
    update_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

CREATE INDEX multi_key_status_time ON `order` (status, create_time);

CREATE TABLE `order_detail`
(
    id          BIGINT AUTO_INCREMENT COMMENT '订单详情id' PRIMARY KEY,
    order_id    BIGINT                                  NOT NULL COMMENT '订单id',
    item_id     BIGINT                                  NOT NULL COMMENT 'sku商品id',
    num         INT                                     NOT NULL COMMENT '购买数量',
    name        VARCHAR(256)                            NOT NULL COMMENT '商品标题',
    spec        VARCHAR(1024) DEFAULT ''                NULL COMMENT '商品动态属性键值集',
    price       INT                                     NOT NULL COMMENT '价格,单位：分',
    image       VARCHAR(256)  DEFAULT ''                NULL COMMENT '商品图片',
    create_time TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '订单详情表' CHARSET = utf8mb4 ROW_FORMAT = COMPACT;

CREATE INDEX key_order_id ON order_detail (order_id);

CREATE TABLE `order_logistics`
(
    order_id          BIGINT                                NOT NULL COMMENT '订单id，与订单表一对一' PRIMARY KEY,
    logistics_number  VARCHAR(18) DEFAULT ''                NULL COMMENT '物流单号',
    logistics_company VARCHAR(18) DEFAULT ''                NULL COMMENT '物流公司名称',
    contact           VARCHAR(32)                           NOT NULL COMMENT '收件人',
    mobile            VARCHAR(11)                           NOT NULL COMMENT '收件人手机号码',
    province          VARCHAR(16)                           NOT NULL COMMENT '省',
    city              VARCHAR(32)                           NOT NULL COMMENT '市',
    town              VARCHAR(32)                           NOT NULL COMMENT '区',
    street            VARCHAR(256)                          NOT NULL COMMENT '街道',
    create_time       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) CHARSET = utf8mb4 ROW_FORMAT = COMPACT;
