package com.wei.mall.trade.domain.dto;

import com.wei.mall.api.dto.OrderDetailDTO;
import lombok.Data;

import java.util.List;

@Data
public class OrderFormDTO {
    private Long addressId;
    private Integer paymentType;
    private List<OrderDetailDTO> details;
}
