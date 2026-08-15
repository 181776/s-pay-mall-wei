package com.wei.mall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.mall.common.exception.BizIllegalException;
import com.wei.mall.common.utils.BeanUtils;
import com.wei.mall.item.domain.dto.ItemDTO;
import com.wei.mall.item.domain.dto.OrderDetailDTO;
import com.wei.mall.item.domain.po.Item;
import com.wei.mall.item.mapper.ItemMapper;
import com.wei.mall.item.service.IItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;


@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {


    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        for (OrderDetailDTO orderDetailDTO : items) {
            int rows = baseMapper.updateStock(orderDetailDTO);
            if (rows == 0) {
                throw new BizIllegalException("库存不足，商品id：" + orderDetailDTO.getItemId());
            }
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    @Transactional
    public void restoreStock(List<OrderDetailDTO> items) {
        for (OrderDetailDTO orderDetailDTO : items) {
            int rows = baseMapper.restoreStock(orderDetailDTO);
            if (rows == 0) {
                throw new BizIllegalException("恢复库存失败，商品id：" + orderDetailDTO.getItemId());
            }
        }

    }
}
