import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
    { path: '/search', name: 'search', component: () => import('../views/SearchView.vue') },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/CartView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/order-confirm',
      name: 'orderConfirm',
      component: () => import('../views/OrderConfirmView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/pay',
      name: 'pay',
      component: () => import('../views/PayView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/pay-success',
      name: 'paySuccess',
      component: () => import('../views/PaySuccessView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      userStore.setReturnUrl(to.fullPath)
      return '/login'
    }
  }
  return true
})

export default router
