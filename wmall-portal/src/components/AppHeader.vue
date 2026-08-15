<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'
import http from '../api/http'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const cartCount = ref(0)

const isLogin = computed(() => userStore.isLogin)

onMounted(async () => {
  if (userStore.isLogin) {
    try {
      const carts = await http.get('/carts')
      cartCount.value = Array.isArray(carts) ? carts.length : 0
    } catch {
      cartCount.value = 0
    }
  }
})

function goLogin() {
  userStore.setReturnUrl(router.currentRoute.value.fullPath)
  router.push('/login')
}

function logout() {
  userStore.logout()
}
</script>

<template>
  <header class="site-header">
    <div class="top-bar">
      <div class="page-container top-inner">
        <div class="top-left">
          <span>WMall 欢迎您！</span>
          <template v-if="!isLogin">
            <a class="link" @click.prevent="goLogin">请登录</a>
            <span class="sep">|</span>
            <a class="link" href="#">免费注册</a>
          </template>
          <template v-else>
            <span class="username">{{ userStore.username }}</span>
            <a class="link" @click.prevent="logout">退出登录</a>
          </template>
        </div>
        <div class="top-right">
          <router-link to="/">首页</router-link>
          <span class="sep">|</span>
          <router-link to="/cart">我的购物车</router-link>
          <span class="sep">|</span>
          <span>会员中心</span>
          <span class="sep">|</span>
          <span>客户服务</span>
        </div>
      </div>
    </div>

    <div class="brand-bar page-container">
      <router-link to="/" class="brand">WMall</router-link>
      <div class="brand-sub">微商城</div>
      <div class="brand-actions">
        <router-link to="/cart" class="cart-btn">
          <el-badge :value="cartCount" :hidden="cartCount === 0" :max="99">
            <el-button type="danger" :icon="ShoppingCart">购物车</el-button>
          </el-badge>
        </router-link>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header {
  background: #fff;
  border-bottom: 1px solid var(--wm-border);
}

.top-bar {
  background: #e3e4e5;
  font-size: 12px;
  color: #666;
}

.top-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}

.top-left,
.top-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.link {
  cursor: pointer;
  color: var(--wm-primary);
}

.username {
  color: var(--wm-primary);
  font-weight: 600;
}

.sep {
  color: #ccc;
}

.brand-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 20px;
  padding-bottom: 20px;
}

.brand {
  font-size: 32px;
  font-weight: 800;
  color: var(--wm-primary);
  letter-spacing: 1px;
}

.brand-sub {
  color: var(--wm-muted);
  font-size: 14px;
}

.brand-actions {
  margin-left: auto;
}
</style>
