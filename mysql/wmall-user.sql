-- wmall-user 用户库
CREATE DATABASE IF NOT EXISTS `wmall-user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `wmall-user`;

DROP TABLE IF EXISTS `address`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user`
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)   NOT NULL COMMENT '用户名',
    password    VARCHAR(128)  NOT NULL COMMENT '密码，加密存储',
    phone       VARCHAR(20)   NULL COMMENT '注册手机号',
    openid      VARCHAR(64)   NULL COMMENT '微信 openid',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    update_time DATETIME      NOT NULL,
    status      INT DEFAULT 1 NULL COMMENT '使用状态（1正常 2冻结）',
    balance     INT           NULL COMMENT '账户余额',
    CONSTRAINT uk_openid UNIQUE (openid),
    CONSTRAINT username UNIQUE (username)
) COMMENT '用户表' CHARSET = utf8mb4 ROW_FORMAT = COMPACT;

CREATE TABLE `address`
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NULL COMMENT '用户ID',
    province   VARCHAR(10)  NULL COMMENT '省',
    city       VARCHAR(10)  NULL COMMENT '市',
    town       VARCHAR(10)  NULL COMMENT '县/区',
    mobile     VARCHAR(255) NULL COMMENT '手机',
    street     VARCHAR(255) NULL COMMENT '详细地址',
    contact    VARCHAR(255) NULL COMMENT '联系人',
    is_default VARCHAR(1)   NULL COMMENT '是否是默认 1默认 0否',
    notes      VARCHAR(255) NULL COMMENT '备注'
) CHARSET = utf8mb4 ROW_FORMAT = COMPACT;

CREATE INDEX user_id ON address (user_id);

-- 演示数据（密码均为 123，BCrypt 加密）
INSERT INTO `user` (id, username, password, phone, openid, create_time, update_time, status, balance)
VALUES (1, 'Jack', '$2a$10$6ptTq3V9XfaJmFYwYT2W9ud377BUkEWk.whf.iQ.0sX5F.L497rAC', '13900112224', NULL,
        '2017-08-19 20:50:21', '2017-08-19 20:50:21', 1, 1000000),
       (2, 'Rose', '$2a$10$6ptTq3V9XfaJmFYwYT2W9ud377BUkEWk.whf.iQ.0sX5F.L497rAC', '13900112223', NULL,
        '2017-08-19 21:00:23', '2017-08-19 21:00:23', 1, 1000000);

INSERT INTO `address` (id, user_id, province, city, town, mobile, street, contact, is_default, notes)
VALUES (61, 1, '上海', '上海', '浦东新区', '13301212233', '航头镇航头路', '李佳星', '1', NULL),
       (63, 1, '广东', '佛山', '永春', '13301212233', '永春武馆', '李小龙', '0', NULL);
