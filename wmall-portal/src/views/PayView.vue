<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { displayPrice } from '../utils/price'

const route = useRoute()
const router = useRouter()

const order = ref({ id: null, totalFee: 0, paymentType: 1 })
const tab = ref(1)
const password = ref('')
const remainTime = ref('')
const payOrderNo = ref('')
const payMsg = ref('')
let pollTimer = null
let countdownTimer = null

function getPayType() {
  if (tab.value === 3) return 5
  if (tab.value === 1) return 6
  return 4
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function startOrderPoll() {
  stopPoll()
  pollTimer = setInterval(async () => {
    try {
      const resp = await http.get(`/orders/${order.value.id}`)
      if (resp.status === 2) {
        stopPoll()
        router.push({ path: '/pay-success', query: { orderId: order.value.id } })
      } else if (resp.status === 5) {
        stopPoll()
        ElMessage.warning('订单已关闭')
      }
    } catch {
      /* ignore poll errors */
    }
  }, 3000)
}

function openAlipayHtml(html) {
  const payWin = window.open('', '_blank')
  if (payWin) {
    payWin.document.open()
    payWin.document.write(html)
    payWin.document.close()
  } else {
    document.open()
    document.write(html)
    document.close()
  }
}

async function payByAlipay() {
  payMsg.value = ''
  try {
    const html = await http.post(
      '/pay-orders',
      {
        bizOrderNo: order.value.id,
        amount: order.value.totalFee,
        payType: 6,
        orderInfo: 'WMall 商品',
        payChannelCode: 'aliPay',
      },
      { transformResponse: [(data) => data] },
    )
    openAlipayHtml(html)
    startOrderPoll()
    payMsg.value = '已打开支付宝收银台，请在新窗口完成支付。'
  } catch {
    ElMessage.error('创建支付单失败，请重试')
  }
}

async function createBalancePayOrder() {
  try {
    payOrderNo.value = await http.post(
      '/pay-orders',
      {
        bizOrderNo: order.value.id,
        amount: order.value.totalFee,
        payType: 5,
        orderInfo: 'WMall 商品',
        payChannelCode: 'balance',
      },
      { transformResponse: [(data) => data] },
    )
  } catch (err) {
    console.error(err)
  }
}

async function payByBalance() {
  if (!payOrderNo.value) {
    ElMessage.warning('交易单号为空，请切换支付方式后重试')
    return
  }
  try {
    await http.post(
      `/pay-orders/${payOrderNo.value}`,
      { id: payOrderNo.value, pw: password.value },
      { transformResponse: [(data) => data] },
    )
    router.push({ path: '/pay-success', query: { orderId: order.value.id } })
  } catch {
    ElMessage.error('支付失败，请重试')
  }
}

function startCountdown(createTime) {
  const deadLine = new Date(createTime).getTime() + 1800000
  countdownTimer = setInterval(() => {
    const remain = deadLine - Date.now()
    if (remain <= 0) {
      clearInterval(countdownTimer)
      ElMessage.warning('支付超时')
      return
    }
    remainTime.value = `${Math.floor(remain / 60000)}分${Math.floor((remain % 60000) / 1000)}秒`
  }, 1000)
}

watch(tab, (val) => {
  if (val === 3) createBalancePayOrder()
})

onMounted(async () => {
  const orderId = route.query.id
  if (!orderId) {
    ElMessage.error('订单号不存在')
    router.push('/')
    return
  }
  try {
    order.value = await http.get(`/orders/${orderId}`)
    tab.value = order.value.paymentType || 1
    if (tab.value === 3) await createBalancePayOrder()
    startCountdown(order.value.createTime)
  } catch {
    ElMessage.error('订单查询失败')
    router.push('/')
  }
})

onBeforeUnmount(() => {
  stopPoll()
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">收银台</h2>
    <div class="card-panel">
      <p>
        订单提交成功，订单号：{{ order.id }}。请在
        <strong>{{ remainTime }}</strong>
        内完成支付。
      </p>
      <p class="amount">应付金额：<span class="price">¥{{ displayPrice(order.totalFee) }}</span></p>

      <el-tabs v-model="tab">
        <el-tab-pane label="支付宝" :name="1">
          <p>将跳转至支付宝沙箱完成支付，支付完成后本页会自动跳转。</p>
          <p v-if="payMsg" class="tip">{{ payMsg }}</p>
          <el-button type="danger" @click="payByAlipay">前往支付宝支付</el-button>
        </el-tab-pane>
        <el-tab-pane label="微信" :name="2">
          <p>暂不支持微信扫码支付，请使用支付宝或余额。</p>
        </el-tab-pane>
        <el-tab-pane label="余额" :name="3">
          <el-input v-model="password" type="password" placeholder="请输入支付密码" style="max-width: 240px; margin-right: 12px" />
          <el-button type="danger" @click="payByBalance">确认支付</el-button>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.amount {
  font-size: 18px;
  margin: 16px 0;
}

.tip {
  color: var(--wm-primary);
}
</style>
