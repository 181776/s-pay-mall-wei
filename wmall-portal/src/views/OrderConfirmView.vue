<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { storage } from '../utils/storage'
import { displayPrice } from '../utils/price'

const router = useRouter()

const addressList = ref([])
const items = ref([])
const params = ref({
  details: [],
  paymentType: 1,
  addressId: null,
})

const fallbackAddresses = [
  { id: 61, contact: '张三', mobile: '13301212233', province: '上海', city: '上海', town: '浦东新区', street: '示例路 1 号', isDefault: true },
  { id: 63, contact: '李四', mobile: '13301212234', province: '广东', city: '佛山', town: '禅城', street: '示例街 2 号', isDefault: false },
]

function parseSpec(str) {
  if (!str) return {}
  try {
    return JSON.parse(str)
  } catch {
    return {}
  }
}

function maskMobile(mobile) {
  if (!mobile || mobile.length < 11) return mobile
  return mobile.substring(0, 3) + '****' + mobile.substring(7)
}

async function getAddress() {
  try {
    const resp = await http.get('/addresses')
    addressList.value = resp?.length ? resp : fallbackAddresses
  } catch {
    addressList.value = fallbackAddresses
  }
  const defaultAddr = addressList.value.find((a) => a.isDefault) || addressList.value[0]
  params.value.addressId = defaultAddr?.id ?? null
}

const addressText = computed(() => {
  const addr = addressList.value.find((a) => a.id === params.value.addressId)
  if (!addr) return ''
  return `${addr.province}${addr.city}${addr.town}${addr.street} 收件人：${addr.contact} ${maskMobile(addr.mobile)}`
})

const totalPrice = computed(() => {
  const total = items.value.reduce((sum, i) => sum + (i.newPrice || i.price) * i.num, 0)
  return displayPrice(total)
})

function incNum(item) {
  if (item.num < item.stock) item.num++
}

function decNum(item) {
  if (item.num > 1) item.num--
}

async function submitOrder() {
  params.value.details = items.value.map(({ itemId, num }) => ({ itemId, num }))
  try {
    const orderId = await http.post('/orders', params.value)
    router.push({ path: '/pay', query: { id: orderId } })
  } catch {
    ElMessage.error('下单失败，请重试')
  }
}

onMounted(async () => {
  items.value = storage.get('selectedCarts') || []
  if (!items.value.length) {
    ElMessage.warning('请先选择商品')
    router.push('/cart')
    return
  }
  await getAddress()
})
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">确认订单</h2>

    <div class="card-panel section">
      <h3>收货地址</h3>
      <div class="address-list">
        <div
          v-for="addr in addressList"
          :key="addr.id"
          class="address-item"
          :class="{ selected: params.addressId === addr.id }"
          @click="params.addressId = addr.id"
        >
          <strong>{{ addr.contact }}</strong>
          <span>{{ addr.province }}{{ addr.city }}{{ addr.town }}{{ addr.street }}</span>
          <span>{{ maskMobile(addr.mobile) }}</span>
          <el-tag v-if="addr.isDefault" size="small" type="danger">默认</el-tag>
        </div>
      </div>
    </div>

    <div class="card-panel section">
      <h3>商品清单</h3>
      <div v-for="item in items" :key="item.id" class="goods-row">
        <img :src="item.image" alt="" />
        <div class="info">
          <div>{{ item.name }}</div>
          <div class="spec">
            <span v-for="(v, k) in parseSpec(item.spec)" :key="k">{{ k }}: {{ v }}</span>
          </div>
        </div>
        <div class="price">¥{{ displayPrice(item.newPrice || item.price) }}</div>
        <div class="qty">
          <el-button size="small" @click="decNum(item)">-</el-button>
          <span>{{ item.num }}</span>
          <el-button size="small" @click="incNum(item)">+</el-button>
        </div>
      </div>
    </div>

    <div class="card-panel summary">
      <div>{{ items.length }} 件商品，应付金额：<span class="price">¥{{ totalPrice }}</span></div>
      <div class="ship">寄送至：{{ addressText }}</div>
      <el-button type="danger" size="large" @click="submitOrder">提交订单</el-button>
    </div>
  </div>
</template>

<style scoped>
.section {
  margin-bottom: 12px;
}

.section h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-item {
  border: 1px solid var(--wm-border);
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.address-item.selected {
  border-color: var(--wm-primary);
  background: #fff5f5;
}

.goods-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--wm-border);
}

.goods-row:last-child {
  border-bottom: none;
}

.goods-row img {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.info {
  flex: 1;
}

.spec {
  font-size: 12px;
  color: var(--wm-muted);
  margin-top: 4px;
}

.qty {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.ship {
  font-size: 13px;
  color: var(--wm-muted);
}
</style>
