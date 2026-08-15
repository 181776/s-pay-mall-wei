import axios from 'axios'
import router from '../router'
import { useUserStore } from '../stores/user'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.authorization = token
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (err) => {
    if (err.response?.status === 401) {
      const url = err.config?.url || ''
      const onLogin = router.currentRoute.value.path === '/login'
      if (url.includes('/users/weixin/') || onLogin) {
        return Promise.reject(err)
      }
      const userStore = useUserStore()
      userStore.clearLogin()
      router.push('/login')
    }
    return Promise.reject(err)
  },
)

export default http
