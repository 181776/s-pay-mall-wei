package com.wei.mall.cart.listener;

import com.wei.mall.api.dto.CartClearMessage;
import com.wei.mall.cart.service.ICartService;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CartStatusListener {

    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic"),
            key = "order.create"
    ))
    void removeByItemIds(CartClearMessage cartClearMessage){
        cartService.removeByItemIds(cartClearMessage);
    }
}