package com.wei.mall.item.domain.query;

import com.wei.mall.common.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemPageQuery extends PageQuery {
    private String key;
    private String category;
    private String brand;
    private Integer minPrice;
    private Integer maxPrice;
}
