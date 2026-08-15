-- wmall-cart 购物车库
CREATE DATABASE IF NOT EXISTS `wmall-cart` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `wmall-cart`;

DROP TABLE IF EXISTS `cart`;

CREATE TABLE `cart`
(
    id          BIGINT AUTO_INCREMENT COMMENT '购物车条目id' PRIMARY KEY,
    user_id     BIGINT                                 NOT NULL COMMENT '用户id',
    item_id     BIGINT                                 NOT NULL COMMENT 'sku商品id',
    num         INT          DEFAULT 1                 NOT NULL COMMENT '购买数量',
    name        VARCHAR(256)                           NOT NULL COMMENT '商品标题',
    spec        VARCHAR(1024)                          NULL COMMENT '商品动态属性键值集',
    price       INT                                    NOT NULL COMMENT '价格,单位：分',
    image       VARCHAR(256) DEFAULT ''                NULL COMMENT '商品图片',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '购物车表' CHARSET = utf8mb4 ROW_FORMAT = COMPACT;

CREATE INDEX key_user_item_id ON cart (user_id, item_id);
