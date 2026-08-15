package com.wei.mall.pay.service;

import com.alipay.api.AlipayApiException;
import com.wei.mall.pay.domain.dto.PayApplyDTO;
import com.wei.mall.pay.domain.dto.PayOrderFormDTO;
import com.wei.mall.pay.domain.po.PayOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IPayOrderService extends IService<PayOrder> {

    String applyPayOrder(PayApplyDTO applyDTO);

    String alipay(PayApplyDTO applyDTO) throws AlipayApiException;

    void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO);

    void handleAlipayNotify(String outTradeNo);

    List<String> queryNoPayNotifyOrder();
}
