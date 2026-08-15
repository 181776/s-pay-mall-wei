import { defineStore } from 'pinia'
import { storage } from '../utils/storage'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: sessionStorage.getItem('token') || '',
    user: storage.get('user-info'),
  }),
  getters: {
    isLogin: (state) => !!state.user,
    username: (state) => state.user?.username || '',
    balance: (state) => state.user?.balance ?? 0,
  },
  actions: {
    saveLogin(vo) {
      this.token = vo.token
      this.user = {
        userId: vo.userId,
        username: vo.username,
        balance: vo.balance,
      }
      sessionStorage.setItem('token', vo.token)
      storage.set('user-info', this.user)
    },
    clearLogin() {
      this.token = ''
      this.user = null
      sessionStorage.removeItem('token')
      storage.del('user-info')
      storage.del('return-url')
    },
    logout() {
      this.clearLogin()
      window.location.reload()
    },
    setReturnUrl(url) {
      storage.set('return-url', url)
    },
    consumeReturnUrl(defaultPath = '/') {
      const url = storage.get('return-url')
      storage.del('return-url')
      if (url && !String(url).includes('/login')) {
        return url
      }
      return defaultPath
    },
  },
})
