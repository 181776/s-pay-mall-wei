package com.wei.mall.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

// hm-api/src/main/java/com/hmall/api/dto/CartClearMessage.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartClearMessage {
    private Long userId;
    private Collection<Long> itemIds;
}