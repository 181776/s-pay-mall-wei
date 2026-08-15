package com.wei.mall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.mall.user.domain.dto.LoginFormDTO;
import com.wei.mall.user.domain.po.User;
import com.wei.mall.user.domain.vo.UserLoginVO;

public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);

    UserLoginVO loginByOpenid(String openid);
}
