package com.wei.mall.cart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.mall.api.dto.CartClearMessage;
import com.wei.mall.cart.domain.dto.CartFormDTO;
import com.wei.mall.cart.domain.po.Cart;
import com.wei.mall.cart.domain.vo.CartVO;


import java.util.Collection;
import java.util.List;

public interface ICartService extends IService<Cart> {

    void addItem2Cart(CartFormDTO cartFormDTO);

    List<CartVO> queryMyCarts();

    void removeByItemIds(CartClearMessage cartClearMessage);

    void removeByItemIds(Collection<Long> itemIds);
}
