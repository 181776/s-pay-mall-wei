<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const keyword = ref('')

function search() {
  router.push({ path: '/search', query: keyword.value ? { key: keyword.value } : {} })
}
</script>

<template>
  <div class="home">
    <section class="hero page-container">
      <h1>WMall 微商城</h1>
      <p class="subtitle">精选好物，品质生活</p>
      <div class="search-box">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="搜索商品"
          @keyup.enter="search"
        />
        <el-button type="danger" size="large" :icon="Search" @click="search">搜索</el-button>
      </div>
      <div class="hotwords">
        <span
          v-for="word in ['手机', '笔记本', '耳机', '拉杆箱', '运动鞋']"
          :key="word"
          class="hotword"
          @click="keyword = word; search()"
        >{{ word }}</span>
      </div>
    </section>

    <section class="page-container categories">
      <h2 class="section-title">热门分类</h2>
      <div class="category-grid">
        <div v-for="c in ['数码家电', '服饰鞋包', '食品生鲜', '图书文娱', '家居日用', '运动户外']" :key="c" class="category-item">
          {{ c }}
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero {
  text-align: center;
  padding: 48px 16px 32px;
}

.hero h1 {
  margin: 0;
  font-size: 36px;
  color: var(--wm-primary);
}

.subtitle {
  color: var(--wm-muted);
  margin: 8px 0 24px;
}

.search-box {
  display: flex;
  gap: 8px;
  max-width: 640px;
  margin: 0 auto 16px;
}

.hotwords {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}

.hotword {
  cursor: pointer;
  color: #666;
  font-size: 13px;
}

.hotword:hover {
  color: var(--wm-primary);
}

.categories {
  margin-top: 8px;
}

.section-title {
  font-size: 18px;
  margin: 0 0 16px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.category-item {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.category-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  color: var(--wm-primary);
}
</style>
