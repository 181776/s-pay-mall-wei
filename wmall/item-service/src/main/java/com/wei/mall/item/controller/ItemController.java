package com.wei.mall.item.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.mall.common.domain.PageDTO;
import com.wei.mall.common.domain.PageQuery;
import com.wei.mall.common.utils.BeanUtils;
import com.wei.mall.item.domain.dto.ItemDTO;
import com.wei.mall.item.domain.dto.OrderDetailDTO;
import com.wei.mall.item.service.IItemService;
import com.wei.mall.item.domain.po.Item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final IItemService itemService;

    @GetMapping("/page")
    public PageDTO<ItemDTO> queryItemByPage(PageQuery query) {
        // 1.分页查询
        Page<Item> result = itemService.page(query.toMpPage("update_time", false));
        // 2.封装并返回
        return PageDTO.of(result, ItemDTO.class);
    }

    @GetMapping
    public List<ItemDTO> queryItemByIds(@RequestParam("ids") List<Long> ids){
        return itemService.queryItemByIds(ids);
    }

    @GetMapping("{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id) {
        return BeanUtils.copyBean(itemService.getById(id), ItemDTO.class);
    }

    @PostMapping
    public void saveItem(@RequestBody ItemDTO item) {
        itemService.save(BeanUtils.copyBean(item, Item.class));
    }

    @PutMapping("/status/{id}/{status}")
    public void updateItemStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status){
        Item item = new Item();
        item.setId(id);
        item.setStatus(status);
        itemService.updateById(item);
    }

    @PutMapping
    public void updateItem(@RequestBody ItemDTO item) {
        // 不允许修改商品状态，所以强制设置为null，更新时，就会忽略该字段
        item.setStatus(null);
        // 更新
        itemService.updateById(BeanUtils.copyBean(item, Item.class));
    }

    @DeleteMapping("{id}")
    public void deleteItemById(@PathVariable("id") Long id) {
        itemService.removeById(id);
    }

    @PutMapping("/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items){
        itemService.deductStock(items);
    }

    @PutMapping("/stock/restore")
    public void restoreStock(@RequestBody List<OrderDetailDTO> items){
        itemService.restoreStock(items);
    }
}
