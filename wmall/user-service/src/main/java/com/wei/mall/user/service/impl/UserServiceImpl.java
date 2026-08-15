package com.wei.mall.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.mall.common.exception.BadRequestException;
import com.wei.mall.common.exception.BizIllegalException;
import com.wei.mall.common.exception.ForbiddenException;
import com.wei.mall.common.utils.UserContext;

import cn.hutool.core.util.StrUtil;
import com.wei.mall.user.config.JwtProperties;
import com.wei.mall.user.domain.dto.LoginFormDTO;
import com.wei.mall.user.domain.po.User;
import com.wei.mall.user.domain.vo.UserLoginVO;
import com.wei.mall.user.enums.UserStatus;
import com.wei.mall.user.mapper.UserMapper;
import com.wei.mall.user.service.IUserService;
import com.wei.mall.user.util.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;

    @Override
    public UserLoginVO login(LoginFormDTO loginDTO) {
        // 1.数据校验
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        // 2.根据用户名或手机号查询
        User user = lambdaQuery().eq(User::getUsername, username).one();
        Assert.notNull(user, "用户名错误");
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        // 4.校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        // 5.生成TOKEN
        String token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());
        return buildLoginVO(user, token);
    }

    @Override
    public void deductMoney(String pw, Integer totalFee) {
        log.info("开始扣款");
        // 1.校验密码
        User user = getById(UserContext.getUser());
        if (user == null || !passwordEncoder.matches(pw, user.getPassword())) {
            // 密码错误
            throw new BizIllegalException("用户密码错误");
        }

        // 2.尝试扣款
        try {
            baseMapper.updateMoney(UserContext.getUser(), totalFee);
        } catch (Exception e) {
            throw new RuntimeException("扣款失败，可能是余额不足！", e);
        }
        log.info("扣款成功");
    }

    @Override
    public UserLoginVO loginByOpenid(String openid) {
        if (StrUtil.isBlank(openid)) {
            throw new BadRequestException("openid无效");
        }
        // 1. 按 openid 查用户
        User user = lambdaQuery().eq(User::getOpenid, openid).one();
        // 2. 首次微信登录则自动注册
        if (user == null) {
            user = new User()
                    .setOpenid(openid)
                    .setUsername("wx_" + openid.substring(Math.max(0, openid.length() - 8)))
                    .setPassword(passwordEncoder.encode(openid))
                    .setStatus(UserStatus.NORMAL)
                    .setBalance(0)
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now());
            save(user);
        }
        // 3. 校验状态
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        // 4. 签发 JWT（与密码登录同一套）
        String token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());
        return buildLoginVO(user, token);
    }

    private UserLoginVO buildLoginVO(User user, String token) {
        return UserLoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .balance(user.getBalance())
                .build();
    }
}
