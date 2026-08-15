<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { storage } from '../utils/storage'
import { displayPrice } from '../utils/price'

const router = useRouter()
const carts = ref([])
const selectedIds = ref([])

const totalPrice = computed(() =>
  carts.value
    .filter((c) => selectedIds.value.includes(c.id))
    .reduce((sum, c) => sum + c.num * (c.newPrice || c.price), 0),
)

const selectedCarts = computed(() => carts.value.filter((c) => selectedIds.value.includes(c.id)))

function parseSpec(spec) {
  if (!spec) return {}
  try {
    return JSON.parse(spec)
  } catch {
    return {}
  }
}

async function loadCarts() {
  try {
    const resp = await http.get('/carts')
    if (!resp?.length) {
      ElMessage.warning('购物车是空的，去看看商品吧')
      router.push('/search')
      return
    }
    carts.value = resp
    refreshSelected()
  } catch {
    ElMessage.error('查询购物车失败')
  }
}

function refreshSelected() {
  selectedIds.value = carts.value.filter((c) => c.status === 1 && c.stock >= c.num).map((c) => c.id)
}

const enableIds = computed(() =>
  carts.value.filter((c) => c.status === 1 && c.stock >= c.num).map((c) => c.id),
)

const selectAll = computed({
  get() {
    return enableIds.value.length > 0 && selectedIds.value.length === enableIds.value.length
  },
  set(val) {
    selectedIds.value = val ? [...enableIds.value] : []
  },
})

async function increment(c) {
  if (c.num >= c.stock) {
    ElMessage.warning('超出库存上限')
    return
  }
  await http.put('/carts', { id: c.id, num: c.num + 1 })
  await loadCarts()
}

async function decrement(c) {
  if (c.num <= 1) return
  await http.put('/carts', { id: c.id, num: c.num - 1 })
  await loadCarts()
}

async function deleteCart(id) {
  await http.delete(`/carts/${id}`)
  await loadCarts()
}

function watchNum(c) {
  if (c.num > c.stock) {
    c.num = c.stock
    ElMessage.warning('超出库存上限')
  }
}

function toOrderConfirm() {
  if (!selectedCarts.value.length) {
    ElMessage.warning('至少要选中一件商品')
    return
  }
  storage.set('selectedCarts', selectedCarts.value)
  router.push('/order-confirm')
}

onMounted(loadCarts)
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">购物车</h2>
    <div class="card-panel">
      <el-table :data="carts" style="width: 100%">
        <el-table-column width="50">
          <template #header>
            <el-checkbox v-model="selectAll" />
          </template>
          <template #default="{ row }">
            <el-checkbox
              v-model="selectedIds"
              :label="row.id"
              :disabled="row.status !== 1 || row.stock < row.num"
            />
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="goods-cell">
              <img :src="row.image" alt="" />
              <div>
                <div>{{ row.name }}</div>
                <div class="spec">
                  <span v-for="(v, k) in parseSpec(row.spec)" :key="k">{{ k }}: {{ v }} </span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ displayPrice(row.newPrice || row.price) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="decrement(row)">-</el-button>
            <el-input v-model.number="row.num" style="width: 56px; margin: 0 4px" @blur="watchNum(row)" />
            <el-button size="small" @click="increment(row)">+</el-button>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            ¥{{ displayPrice((row.newPrice || row.price) * row.num) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" @click="deleteCart(row.id)">删除</el-button>
            <div v-if="row.status !== 1" class="warn">已下架</div>
            <div v-if="row.num > row.stock" class="warn">库存不足</div>
          </template>
        </el-table-column>
      </el-table>

      <div class="footer">
        <div>已选 {{ selectedCarts.length }} 件，合计：<span class="price">¥{{ displayPrice(totalPrice) }}</span></div>
        <el-button type="danger" size="large" @click="toOrderConfirm">去结算</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.goods-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.goods-cell img {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.spec {
  font-size: 12px;
  color: var(--wm-muted);
}

.warn {
  color: var(--wm-primary);
  font-size: 12px;
}

.footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--wm-border);
}
</style>
