package com.wei.mall.pay.job;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.wei.mall.pay.service.IPayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoPayNotifyOrderJob {

    private final IPayOrderService payOrderService;
    private final AlipayClient alipayClient;

    @Scheduled(cron = "0 */5 * * * ?")
    public void exec() {
        try {
            List<String> payOrderNos = payOrderService.queryNoPayNotifyOrder();
            if (payOrderNos == null || payOrderNos.isEmpty()) {
                return;
            }
            log.info("查单补偿：待查单 {} 笔", payOrderNos.size());

            for (String payOrderNo : payOrderNos) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(payOrderNo);
                request.setBizModel(bizModel);

                AlipayTradeQueryResponse response = alipayClient.execute(request);
                if ("10000".equals(response.getCode()) && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
                    log.info("查单补偿成功，payOrderNo={}", payOrderNo);
                    payOrderService.handleAlipayNotify(payOrderNo);
                    continue;
                }
                if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                    log.debug("查单：支付宝侧暂无交易 payOrderNo={}", payOrderNo);
                    continue;
                }
                log.warn("查单：payOrderNo={} 状态异常 code={} subCode={} subMsg={}",
                        payOrderNo, response.getCode(), response.getSubCode(), response.getSubMsg());
            }
        } catch (Exception e) {
            log.error("查单补偿任务执行失败", e);
        }
    }
}
