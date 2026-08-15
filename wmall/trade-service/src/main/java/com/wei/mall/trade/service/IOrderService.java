package com.wei.mall.trade.service;

import com.wei.mall.trade.domain.dto.OrderFormDTO;
import com.wei.mall.trade.domain.po.Order;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    void cancelOrder(Long orderId);
}
