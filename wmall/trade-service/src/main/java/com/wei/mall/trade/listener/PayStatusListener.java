package com.wei.mall.trade.listener;

import com.wei.mall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        orderService.markOrderPaySuccess(orderId);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.order.delay.queue", durable = "true"),
            exchange = @Exchange(
                    name = "delay.direct",
                    type = "x-delayed-message",
                    arguments = @Argument(name = "x-delayed-type", value = "direct")
            ),
            key = "order.delay"
    ))
    public void listenOrderDelay(Long orderId) {
        orderService.cancelOrder(orderId);
    }
}