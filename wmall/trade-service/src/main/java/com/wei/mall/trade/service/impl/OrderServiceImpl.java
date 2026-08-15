package com.wei.mall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.mall.api.client.ItemClient;
import com.wei.mall.api.dto.CartClearMessage;
import com.wei.mall.common.exception.BadRequestException;
import com.wei.mall.common.utils.UserContext;
import com.wei.mall.api.dto.ItemDTO;
import com.wei.mall.api.dto.OrderDetailDTO;
import com.wei.mall.trade.domain.dto.OrderFormDTO;
import com.wei.mall.trade.domain.po.Order;
import com.wei.mall.trade.domain.po.OrderDetail;
import com.wei.mall.trade.mapper.OrderMapper;

import com.wei.mall.trade.service.IOrderDetailService;
import com.wei.mall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {


    private final ItemClient itemClient;
    private final IOrderDetailService detailService;


    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();

        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);



        // 3.清理购物车商品
        CartClearMessage cartClearMessage = new CartClearMessage(order.getUserId(), itemIds);
        rabbitTemplate.convertAndSend("trade.topic", "order.create", cartClearMessage);

        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }

        //自发消息
        rabbitTemplate.convertAndSend("delay.direct", "order.delay", order.getId(), message -> {
            message.getMessageProperties().setDelay(1800000); // 30 分钟
            return message;
        });
        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }

    @Override
    public void cancelOrder(Long orderId) {
        if (orderId == null) {
            return;
        }
        Order order = getById(orderId);
        if (order == null) {
            return;
        }
        if (order.getStatus() == 5) {
            return;
        }
        if (order.getStatus() != 1) {
            return;
        }
        order.setStatus(5);
        order.setCloseTime(LocalDateTime.now());
        updateById(order);

        //  查该订单下的所有明细
        List<OrderDetail> details = detailService.lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId)
                .list();

        // 如果没有明细，就不用恢复库存了
        if (details == null || details.isEmpty()) {
            return;
        }
        //  PO 转成 DTO（只需要 itemId 和 num）
        List<OrderDetailDTO> items = details.stream().map(d -> {
            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setItemId(d.getItemId());
            dto.setNum(d.getNum());
            return dto;
        }).collect(Collectors.toList());

        itemClient.restoreStock(items);
    }

}

