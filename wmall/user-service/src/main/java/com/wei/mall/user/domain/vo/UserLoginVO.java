package com.wei.mall.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO {
    private String token;
    private Long userId;
    private String username;
    private Integer balance;
}
