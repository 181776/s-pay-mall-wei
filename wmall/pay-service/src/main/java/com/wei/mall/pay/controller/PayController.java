package com.wei.mall.pay.controller;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.wei.mall.common.exception.BizIllegalException;
import com.wei.mall.pay.config.AliPayConfig;
import com.wei.mall.pay.domain.dto.PayApplyDTO;
import com.wei.mall.pay.domain.dto.PayOrderFormDTO;
import com.wei.mall.pay.enums.PayType;
import com.wei.mall.pay.service.IPayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("pay-orders")
@RequiredArgsConstructor
public class PayController {

    private final IPayOrderService payOrderService;
    private final AliPayConfig aliPayConfig;

    @PostMapping
    public String applyPayOrder(@RequestBody PayApplyDTO applyDTO){
        if(PayType.ALIPAY.equalsValue(applyDTO.getPayType())){
            try {
                return payOrderService.alipay(applyDTO);
            } catch (AlipayApiException e) {
                throw new BizIllegalException("支付宝预下单失败");
            }
        }
        if(!PayType.BALANCE.equalsValue(applyDTO.getPayType())){
            throw new BizIllegalException("抱歉，目前不支持");
        }
        return payOrderService.applyPayOrder(applyDTO);
    }

    @PostMapping("{id}")
    public void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody PayOrderFormDTO payOrderFormDTO){
        payOrderFormDTO.setId(id);
        payOrderService.tryPayOrderByBalance(payOrderFormDTO);
    }

    @PostMapping("alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        if (!"TRADE_SUCCESS".equals(request.getParameter("trade_status"))) {
            return "failure";
        }

        Map<String, String> params = new HashMap<>();
        for (String name : request.getParameterMap().keySet()) {
            params.put(name, request.getParameter(name));
        }

        String sign = params.get("sign");
        if (StrUtil.isBlank(sign)) {
            return "failure";
        }

        try {
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(
                    content, sign, aliPayConfig.getAlipay_public_key(), "UTF-8");
            if (!checkSignature) {
                return "failure";
            }
        } catch (AlipayApiException e) {
            return "failure";
        }

        String outTradeNo = params.get("out_trade_no");
        payOrderService.handleAlipayNotify(outTradeNo);
        return "success";
    }


}
