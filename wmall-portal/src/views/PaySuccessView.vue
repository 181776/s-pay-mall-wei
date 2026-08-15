<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
import { displayPrice } from '../utils/price'

const route = useRoute()
const router = useRouter()
const order = ref(null)

onMounted(async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    router.push('/')
    return
  }
  try {
    order.value = await http.get(`/orders/${orderId}`)
  } catch {
    router.push('/')
  }
})
</script>

<template>
  <div class="page-container success-page">
    <div class="card-panel" v-if="order">
      <el-result icon="success" title="支付成功" sub-title="感谢您的购买">
        <template #extra>
          <p>订单号：{{ order.id }}</p>
          <p>支付金额：<span class="price">¥{{ displayPrice(order.totalFee) }}</span></p>
          <el-button type="danger" @click="router.push('/search')">继续购物</el-button>
          <el-button @click="router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<style scoped>
.success-page {
  padding-top: 40px;
}
</style>
