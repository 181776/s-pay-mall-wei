-- wmall-item 商品库
CREATE DATABASE IF NOT EXISTS `wmall-item` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `wmall-item`;

DROP TABLE IF EXISTS `item`;

CREATE TABLE `item`
(
    id            BIGINT AUTO_INCREMENT COMMENT '商品id' PRIMARY KEY,
    name          VARCHAR(200)                         NOT NULL COMMENT 'SKU名称',
    price         INT        DEFAULT 0                 NOT NULL COMMENT '价格（分）',
    stock         INT UNSIGNED                         NOT NULL COMMENT '库存数量',
    image         VARCHAR(200)                         NULL COMMENT '商品图片',
    category      VARCHAR(200)                         NULL COMMENT '类目名称',
    brand         VARCHAR(100)                         NULL COMMENT '品牌名称',
    spec          VARCHAR(200)                         NULL COMMENT '规格',
    sold          INT        DEFAULT 0                 NULL COMMENT '销量',
    comment_count INT        DEFAULT 0                 NULL COMMENT '评论数',
    isAD          TINYINT(1) DEFAULT 0                 NULL COMMENT '是否是推广广告，true/false',
    status        INT        DEFAULT 1                 NULL COMMENT '商品状态 1-正常，2-下架，3-删除',
    create_time   DATETIME   DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time   DATETIME   DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creater       BIGINT                               NULL COMMENT '创建人',
    updater       BIGINT                               NULL COMMENT '修改人'
) COMMENT '商品表' CHARSET = utf8mb4 ROW_FORMAT = COMPACT;

CREATE INDEX category ON item (category);
CREATE INDEX status ON item (status);
CREATE INDEX updated ON item (update_time);

-- 演示商品
INSERT INTO `item` (id, name, price, stock, image, category, brand, spec, sold, comment_count, isAD, status,
                    create_time, update_time)
VALUES (100001, '演示商品 A - 无线耳机', 19900, 500,
        'https://m.360buyimg.com/mobilecms/s720x720_jfs/t1/22734/21/2036/130399/5c18af2aEab296c01/7b148f18c6081654.jpg!q70.jpg.webp',
        '数码', 'Demo', '{"颜色": "黑色"}', 128, 32, 1, 1, NOW(), NOW()),
       (100002, '演示商品 B - 旅行拉杆箱 25寸', 36600, 200,
        'https://m.360buyimg.com/mobilecms/s720x720_jfs/t30454/163/719393962/79149/13bcc06a/5bfca9b6N493202d2.jpg!q70.jpg.webp',
        '拉杆箱', 'Demo', '{"颜色": "灰色", "尺寸": "25英寸"}', 56, 10, 0, 1, NOW(), NOW()),
       (100003, '演示商品 C - 运动水杯', 5900, 1000,
        'https://m.360buyimg.com/mobilecms/s720x720_jfs/t6934/364/1195375010/84676/e9f2c55f/597ece38N0ddcbc77.jpg!q70.jpg.webp',
        '户外', 'Demo', '{"容量": "500ml"}', 230, 45, 0, 1, NOW(), NOW()),
       (100004, '演示商品 D - 蓝牙音箱', 8900, 300,
        'https://m.360buyimg.com/mobilecms/s720x720_jfs/t3301/221/3887995271/90563/bf2cadb/57f9fbf4N8e47c225.jpg!q70.jpg.webp',
        '数码', 'Demo', '{"颜色": "蓝色"}', 89, 18, 0, 1, NOW(), NOW()),
       (100005, '演示商品 E - 休闲双肩包', 12900, 150,
        'https://m.360buyimg.com/mobilecms/s720x720_jfs/t1/22734/21/2036/130399/5c18af2aEab296c01/7b148f18c6081654.jpg!q70.jpg.webp',
        '箱包', 'Demo', '{"颜色": "黑色"}', 67, 12, 0, 1, NOW(), NOW());
