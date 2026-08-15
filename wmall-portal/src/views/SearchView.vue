<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import { useUserStore } from '../stores/user'
import { storage } from '../utils/storage'
import { displayPrice } from '../utils/price'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const filterNames = { category: '分类', brand: '品牌', price: '价格' }
const prices = [
  { value: '0-100', label: '100以下' },
  { value: '100-299', label: '100~299元' },
  { value: '300-599', label: '300~599元' },
  { value: '600-899', label: '600~899元' },
  { value: '900-1599', label: '900~1599元' },
  { value: '1600-0', label: '1600以上' },
]
const sortItems = [
  { key: '', text: '默认', desc: true },
  { key: 'sold', text: '销量', desc: true },
  { key: 'price', text: '价格', desc: true },
]

const items = ref([])
const total = ref(0)
const totalPage = ref(0)
const filterList = ref({})
const ops = ref([])
const showOps = ref(false)
const selectedSortItem = ref(0)

const params = ref({
  key: '',
  pageNo: 1,
  pageSize: 20,
  sortBy: '',
  isAsc: true,
  filters: {},
})

const remainFilter = computed(() => {
  const keys = Object.keys(params.value.filters)
  const obj = {}
  Object.keys(filterList.value).forEach((key) => {
    if (!keys.includes(key) && filterList.value[key]?.length > 1) {
      obj[key] = filterList.value[key]
    }
  })
  return obj
})

function getParams() {
  const { filters: { price: ps, ...fs }, ...rest } = params.value
  const result = { ...rest }
  Object.keys(fs).forEach((k) => {
    result[k] = fs[k]
  })
  if (ps) {
    const pArr = ps.value.split('-')
    result.minPrice = parseInt(pArr[0], 10) * 100
    const max = parseInt(pArr[1], 10) * 100
    result.maxPrice = max === 0 ? 999999 : max
  }
  const sortItem = sortItems[selectedSortItem.value]
  result.isAsc = !sortItem.desc
  result.sortBy = sortItem.key
  return result
}

async function getSuggestion() {
  if (!params.value.key) {
    ops.value = []
    return
  }
  try {
    ops.value = await http.get('/search/suggestion', { params: { key: params.value.key } })
  } catch {
    ops.value = []
  }
}

async function getFilter() {
  try {
    filterList.value = await http.post('/search/filters', getParams())
  } catch {
    filterList.value = {}
  }
}

async function search() {
  try {
    const resp = await http.get('/search/list', { params: getParams() })
    items.value = resp.list || []
    total.value = resp.total || 0
    totalPage.value = Math.floor((total.value + params.value.pageSize - 1) / params.value.pageSize) || 0
  } catch {
    items.value = []
    total.value = 0
    totalPage.value = 0
  }
}

function handleSearch() {
  params.value.pageNo = 1
  search()
  getFilter()
}

function handleSort(i) {
  if (i === selectedSortItem.value) {
    sortItems[i].desc = !sortItems[i].desc
  } else {
    selectedSortItem.value = i
  }
  search()
}

function clickFilter(key, option) {
  params.value.filters = { ...params.value.filters, [key]: option }
}

function deleteFilter(k) {
  const next = { ...params.value.filters }
  delete next[k]
  params.value.filters = next
}

function commentCount(count) {
  if (count < 10000) return count
  return `${Math.floor(count / 10000)}万+`
}

async function add2Cart(item) {
  if (!userStore.isLogin) {
    userStore.setReturnUrl(route.fullPath)
    router.push('/login')
    return
  }
  const { id, ...rest } = item
  try {
    await http.post('/carts', { ...rest, itemId: id, num: 1 })
    router.push('/cart')
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '添加购物车失败')
  }
}

function buyNow(item) {
  if (!userStore.isLogin) {
    userStore.setReturnUrl(route.fullPath)
    router.push('/login')
    return
  }
  storage.set('selectedCarts', [{ ...item, itemId: item.id, num: 1 }])
  router.push('/order-confirm')
}

watch(
  () => params.value.pageNo,
  () => search(),
)
watch(
  () => params.value.filters,
  () => {
    params.value.pageNo = 1
    search()
    getFilter()
  },
  { deep: true },
)

onMounted(() => {
  const key = route.query.key
  if (key) params.value.key = String(key)
  search()
  getFilter()
})
</script>

<template>
  <div class="search-page page-container">
    <div class="search-bar card-panel">
      <div class="search-input-wrap">
        <el-input
          v-model="params.key"
          placeholder="输入关键字搜索商品"
          @focus="showOps = true; getSuggestion()"
          @input="getSuggestion()"
          @keyup.enter="handleSearch"
        />
        <el-button type="danger" @click="handleSearch">搜索</el-button>
      </div>
      <div v-if="showOps && ops.length" class="suggestions">
        <div v-for="(op, i) in ops" :key="i" class="suggestion" @click="params.key = op; showOps = false; handleSearch()">
          {{ op }}
        </div>
      </div>
    </div>

    <div v-if="Object.keys(params.filters).length" class="selected-filters card-panel">
      <span>已选：</span>
      <el-tag
        v-for="(v, k) in params.filters"
        :key="k"
        closable
        @close="deleteFilter(k)"
      >
        {{ filterNames[k] }}：{{ v.label || v }}
      </el-tag>
    </div>

    <div v-if="Object.keys(remainFilter).length" class="filters card-panel">
      <div v-for="(options, key) in remainFilter" :key="key" class="filter-row">
        <strong>{{ filterNames[key] }}</strong>
        <div class="filter-options">
          <a v-for="(opt, j) in options" :key="j" href="#" @click.prevent="clickFilter(key, opt)">{{ opt }}</a>
        </div>
      </div>
      <div class="filter-row">
        <strong>价格</strong>
        <div class="filter-options">
          <a v-for="p in prices" :key="p.value" href="#" @click.prevent="clickFilter('price', p)">{{ p.label }}</a>
        </div>
      </div>
    </div>

    <div class="sort-bar card-panel">
      <a
        v-for="(item, i) in sortItems"
        :key="i"
        href="#"
        :class="{ active: selectedSortItem === i }"
        @click.prevent="handleSort(i)"
      >
        {{ item.text }}
        <span v-if="item.key">{{ item.desc ? '▼' : '▲' }}</span>
      </a>
      <div class="pager">
        共 {{ total }} 件
        <el-button size="small" :disabled="params.pageNo <= 1" @click="params.pageNo--">上一页</el-button>
        <span>{{ params.pageNo }} / {{ totalPage || 1 }}</span>
        <el-button size="small" :disabled="params.pageNo >= totalPage" @click="params.pageNo++">下一页</el-button>
      </div>
    </div>

    <div class="item-grid">
      <div v-for="item in items" :key="item.id" class="item-card card-panel">
        <img :src="item.image" :alt="item.name" />
        <div class="price">¥{{ displayPrice(item.price) }}</div>
        <div class="name" v-html="item.name" />
        <div class="meta">{{ commentCount(item.commentCount) }} 条评价 · 月销 {{ commentCount(item.sold) }}</div>
        <div class="actions">
          <el-button size="small" @click="buyNow(item)">立即购买</el-button>
          <el-button type="danger" size="small" @click="add2Cart(item)">加入购物车</el-button>
        </div>
      </div>
    </div>
    <el-empty v-if="!items.length" description="暂无商品" />
  </div>
</template>

<style scoped>
.search-bar {
  position: relative;
  margin-bottom: 12px;
}

.search-input-wrap {
  display: flex;
  gap: 8px;
}

.suggestions {
  position: absolute;
  top: 56px;
  left: 20px;
  right: 20px;
  background: #fff;
  border: 1px solid var(--wm-border);
  z-index: 10;
}

.suggestion {
  padding: 8px 12px;
  cursor: pointer;
}

.suggestion:hover {
  background: #f5f5f5;
}

.selected-filters,
.filters,
.sort-bar {
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--wm-border);
}

.filter-row:last-child {
  border-bottom: none;
}

.filter-row strong {
  width: 48px;
  flex-shrink: 0;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-options a:hover {
  color: var(--wm-primary);
}

.sort-bar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sort-bar a.active {
  color: var(--wm-primary);
  font-weight: 600;
}

.pager {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.item-card img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 4px;
}

.name {
  font-size: 13px;
  height: 40px;
  overflow: hidden;
  margin: 8px 0 4px;
}

.meta {
  font-size: 12px;
  color: var(--wm-muted);
  margin-bottom: 8px;
}

.actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 960px) {
  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
