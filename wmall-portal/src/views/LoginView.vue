<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('password')
const form = ref({ username: '', password: '' })
const msg = ref('')
const qrcodeUrl = ref('')
const wxMsg = ref('')
let pollTimer = null
let currentTicket = ''

function normalizeTicket(ticket) {
  return ticket ? ticket.trim().replace(/ /g, '+') : ticket
}

function saveLogin(vo, fromWeixin = false) {
  if (!vo?.token) return
  userStore.saveLogin(vo)
  const target = fromWeixin ? '/' : userStore.consumeReturnUrl('/')
  router.push(target)
}

async function loadWeixinQrcode() {
  stopPoll()
  try {
    const ticket = normalizeTicket(await http.get('/users/weixin/qrcode-ticket', {
      transformResponse: [(data) => data],
    }))
    currentTicket = ticket
    qrcodeUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${encodeURIComponent(ticket)}`
    wxMsg.value = '请使用微信扫描二维码'
    startWxPoll(ticket)
  } catch {
    wxMsg.value = '二维码加载失败，请刷新页面重试'
  }
}

function startWxPoll(ticket) {
  stopPoll()
  ticket = normalizeTicket(ticket)
  pollTimer = setInterval(async () => {
    try {
      const vo = await http.get('/users/weixin/check-login', { params: { ticket } })
      if (vo?.token) {
        stopPoll()
        wxMsg.value = '登录成功，正在跳转...'
        saveLogin(vo, true)
      }
    } catch (err) {
      if (err.response?.status >= 500) {
        wxMsg.value = '登录校验失败，请查看 user-service 日志'
      }
    }
  }, 3000)
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function login() {
  msg.value = ''
  try {
    const vo = await http.post('/users/login', form.value)
    stopPoll()
    saveLogin(vo)
  } catch {
    msg.value = '用户名或密码错误'
  }
}

onMounted(() => {
  loadWeixinQrcode()
})

onBeforeUnmount(stopPoll)
</script>

<template>
  <div class="login-page page-container">
    <div class="login-card card-panel">
      <h2 class="page-title">登录 WMall</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账户登录" name="password">
          <el-alert v-if="msg" :title="msg" type="error" show-icon :closable="false" style="margin-bottom: 16px" />
          <el-form @submit.prevent="login">
            <el-form-item label="账号">
              <el-input v-model="form.username" placeholder="邮箱/用户名/手机号" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
            <el-button type="danger" style="width: 100%" @click="login">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="微信扫码" name="scan">
          <div class="wx-panel">
            <p>{{ wxMsg || '请使用微信扫描二维码登录' }}</p>
            <img v-if="qrcodeUrl" :src="qrcodeUrl" alt="微信登录二维码" class="qrcode" />
            <el-button link type="primary" @click="loadWeixinQrcode">刷新二维码</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.login-card {
  width: 420px;
}

.wx-panel {
  text-align: center;
  padding: 8px 0 16px;
}

.qrcode {
  width: 200px;
  height: 200px;
  margin: 12px auto;
  display: block;
}
</style>
