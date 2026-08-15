package com.wei.mall.user.domain.req;

import lombok.*;

/**
 * @description 获取微信登录二维码请求对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeixinQrCodeReq {

    private int expire_seconds;//有效时间
    private String action_name;//二维码类型
    private ActionInfo action_info;//详细信息

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActionInfo {
        private Scene scene;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Scene {
            private int scene_id;
            private String scene_str;
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum ActionNameTypeVO {
        QR_SCENE("QR_SCENE", "临时的整型参数值"),
        QR_STR_SCENE("QR_STR_SCENE", "临时的字符串参数值"),
        QR_LIMIT_SCENE("QR_LIMIT_SCENE", "永久的整型参数值"),
        QR_LIMIT_STR_SCENE("QR_LIMIT_STR_SCENE", "永久的字符串参数值");

        private String code;
        private String info;
    }

}