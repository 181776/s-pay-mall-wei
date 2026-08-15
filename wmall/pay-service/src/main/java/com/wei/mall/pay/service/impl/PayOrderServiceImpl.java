package com.wei.mall.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.mall.api.client.UserClient;
import com.wei.mall.common.exception.BizIllegalException;
import com.wei.mall.common.utils.BeanUtils;
import com.wei.mall.common.utils.UserContext;
import com.wei.mall.pay.config.AliPayConfig;
import com.wei.mall.pay.domain.dto.PayApplyDTO;
import com.wei.mall.pay.domain.dto.PayOrderFormDTO;
import com.wei.mall.pay.domain.po.PayOrder;
import com.wei.mall.pay.enums.PayStatus;
import com.wei.mall.pay.enums.PayType;
import com.wei.mall.pay.mapper.PayOrderMapper;
import com.wei.mall.pay.service.IPayOrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


  //支付订单 服务实现类
@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    private final AliPayConfig aliPayConfig;
    private final AlipayClient alipayClient;
    private final RabbitTemplate rabbitTemplate;
    private final UserClient userClient;

    @Override
    //outTradeNO即 payOrderNo
    public void handleAlipayNotify(String outTradeNo){
        PayOrder payOrder = lambdaQuery()
                .eq(PayOrder::getPayOrderNo, Long.valueOf(outTradeNo))
                .one();
        if (payOrder == null) {
            return;
        }
        if (PayStatus.TRADE_SUCCESS.equalsValue(payOrder.getStatus())) {
            return;  // 已支付，幂等
        }
        if (PayStatus.TRADE_CLOSED.equalsValue(payOrder.getStatus())) {
            return;  // 已关闭
        }
        markPayOrderSuccess(payOrder.getId(), LocalDateTime.now());
        rabbitTemplate.convertAndSend("pay.direct", "pay.success", payOrder.getBizOrderNo());

    }

    @Override
    public String alipay(PayApplyDTO applyDTO) throws AlipayApiException {
        PayOrder payOrder = checkIdempotent(applyDTO);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(aliPayConfig.getNotify_url());
        request.setReturnUrl(aliPayConfig.getReturn_url());

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payOrder.getPayOrderNo().toString());
        bizContent.put("total_amount", String.format("%.2f", payOrder.getAmount() / 100.0));
        bizContent.put("subject", applyDTO.getOrderInfo());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(bizContent.toString());
        String html = alipayClient.pageExecute(request).getBody();

        return html;
    }

    @Override
    public String applyPayOrder(PayApplyDTO applyDTO) {
        // 1.幂等性校验
        PayOrder payOrder = checkIdempotent(applyDTO);
        // 2.返回结果
        return payOrder.getId().toString();
    }

    @Override
    @Transactional
    public void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        // 1.查询支付单
        PayOrder po = getById(payOrderFormDTO.getId());
        // 2.判断状态
        if(!PayStatus.WAIT_BUYER_PAY.equalsValue(po.getStatus())){
            // 订单不是未支付，状态异常
            throw new BizIllegalException("交易已支付或关闭！");
        }
        // 3.尝试扣减余额
        userClient.deductMoney(payOrderFormDTO.getPw(), po.getAmount());
        // 4.修改支付单状态
        boolean success = markPayOrderSuccess(payOrderFormDTO.getId(), LocalDateTime.now());
        if (!success) {
            throw new BizIllegalException("交易已支付或关闭！");
        }
        // 5.修改订单状态
        rabbitTemplate.convertAndSend("pay.direct", "pay.success", po.getBizOrderNo());

    }

    public boolean markPayOrderSuccess(Long id, LocalDateTime successTime) {
        return lambdaUpdate()
                .set(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue())
                .set(PayOrder::getPaySuccessTime, successTime)
                .eq(PayOrder::getId, id)
                // 支付状态的乐观锁判断
                .in(PayOrder::getStatus, PayStatus.NOT_COMMIT.getValue(), PayStatus.WAIT_BUYER_PAY.getValue())
                .update();
    }

    private PayOrder checkIdempotent(PayApplyDTO applyDTO) {
        // 1.首先查询支付单
        PayOrder oldOrder = queryByBizOrderNo(applyDTO.getBizOrderNo());
        // 2.判断是否存在
        if (oldOrder == null) {
            // 不存在支付单，说明是第一次，写入新的支付单并返回
            PayOrder payOrder = buildPayOrder(applyDTO);
            payOrder.setPayOrderNo(IdWorker.getId());
            save(payOrder);
            return payOrder;
        }
        // 3.旧单已经存在，判断是否支付成功
        if (PayStatus.TRADE_SUCCESS.equalsValue(oldOrder.getStatus())) {
            // 已经支付成功，抛出异常
            throw new BizIllegalException("订单已经支付！");
        }
        // 4.旧单已经存在，判断是否已经关闭
        if (PayStatus.TRADE_CLOSED.equalsValue(oldOrder.getStatus())) {
            // 已经关闭，抛出异常
            throw new BizIllegalException("订单已关闭");
        }
        // 5.旧单已经存在，判断支付渠道是否一致
        if (!StringUtils.equals(oldOrder.getPayChannelCode(), applyDTO.getPayChannelCode())) {
            // 支付渠道不一致，需要重置数据，然后重新申请支付单
            PayOrder payOrder = buildPayOrder(applyDTO);
            payOrder.setId(oldOrder.getId());
            payOrder.setQrCodeUrl("");
            updateById(payOrder);
            payOrder.setPayOrderNo(oldOrder.getPayOrderNo());
            return payOrder;
        }
        // 6.旧单已经存在，且可能是未支付或未提交，且支付渠道一致，直接返回旧数据
        return oldOrder;
    }

    private PayOrder buildPayOrder(PayApplyDTO payApplyDTO) {
        // 1.数据转换
        PayOrder payOrder = BeanUtils.toBean(payApplyDTO, PayOrder.class);
        // 2.初始化数据
        payOrder.setPayOverTime(LocalDateTime.now().plusMinutes(120L));
        payOrder.setStatus(PayStatus.WAIT_BUYER_PAY.getValue());
        payOrder.setBizUserId(UserContext.getUser());
        return payOrder;
    }
    public PayOrder queryByBizOrderNo(Long bizOrderNo) {
        return lambdaQuery()
                .eq(PayOrder::getBizOrderNo, bizOrderNo)
                .one();
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        return lambdaQuery()
                .select(PayOrder::getPayOrderNo)
                .in(PayOrder::getStatus, PayStatus.NOT_COMMIT.getValue(), PayStatus.WAIT_BUYER_PAY.getValue())
                .eq(PayOrder::getPayType, PayType.ALIPAY.getValue())
                .le(PayOrder::getCreateTime, threshold)
                .orderByAsc(PayOrder::getId)
                .last("LIMIT 50")
                .list()
                .stream()
                .map(payOrder -> payOrder.getPayOrderNo().toString())
                .collect(Collectors.toList());
    }
}
