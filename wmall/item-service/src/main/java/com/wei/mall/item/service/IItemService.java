package com.wei.mall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.mall.item.domain.dto.ItemDTO;
import com.wei.mall.item.domain.dto.OrderDetailDTO;
import com.wei.mall.item.domain.po.Item;


import java.util.Collection;
import java.util.List;

public interface IItemService extends IService<Item> {

    void deductStock(List<OrderDetailDTO> items);

    List<ItemDTO> queryItemByIds(Collection<Long> ids);

    void restoreStock(List<OrderDetailDTO> items);
}
