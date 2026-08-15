package com.wei.mall.user.controller;


import com.wei.mall.common.exception.BadRequestException;
import com.wei.mall.common.utils.BeanUtils;
import com.wei.mall.common.utils.CollUtils;
import com.wei.mall.common.utils.UserContext;

import com.wei.mall.user.domain.dto.AddressDTO;
import com.wei.mall.user.domain.po.Address;
import com.wei.mall.user.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping("{addressId}")
    public AddressDTO findAddressById(@PathVariable("addressId") Long id) {
        // 1.根据id查询
        Address address = addressService.getById(id);
        // 2.判断当前用户
        Long userId = UserContext.getUser();
        if(!address.getUserId().equals(userId)){
            throw new BadRequestException("地址不属于当前登录用户");
        }
        return BeanUtils.copyBean(address, AddressDTO.class);
    }
    @GetMapping
    public List<AddressDTO> findMyAddresses() {
        // 1.查询列表
        List<Address> list = addressService.query().eq("user_id", UserContext.getUser()).list();
        // 2.判空
        if (CollUtils.isEmpty(list)) {
            return CollUtils.emptyList();
        }
        // 3.转vo
        return BeanUtils.copyList(list, AddressDTO.class);
    }
}
