package com.wei.mall.user.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.mall.user.domain.po.Address;
import com.wei.mall.user.mapper.AddressMapper;
import com.wei.mall.user.service.IAddressService;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

}
