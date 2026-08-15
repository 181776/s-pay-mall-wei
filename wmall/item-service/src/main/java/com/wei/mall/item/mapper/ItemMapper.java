package com.wei.mall.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.wei.mall.item.domain.dto.OrderDetailDTO;
import com.wei.mall.item.domain.po.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;



@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    @Update("UPDATE item SET stock = stock - #{num} WHERE id = #{itemId} AND stock >= #{num}")
    int updateStock(OrderDetailDTO orderDetail);


    @Update("UPDATE item SET stock = stock + #{num} WHERE id = #{itemId}")
    int restoreStock(OrderDetailDTO orderDetail);
}
