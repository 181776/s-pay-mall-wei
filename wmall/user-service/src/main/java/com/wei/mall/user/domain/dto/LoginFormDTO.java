package com.wei.mall.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginFormDTO {
    @NotNull(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    private String password;
    private Boolean rememberMe = false;
}
