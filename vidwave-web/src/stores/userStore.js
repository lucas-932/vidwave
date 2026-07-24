import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态：用户信息和 Token
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const avatarUrl = ref(localStorage.getItem('avatarUrl') || '')

  // 是否已登录
  const isLoggedIn = () => {
    return token.value !== ''
  }

  // 登录成功，保存信息
  const setLoginInfo = (newToken, newUsername) => {
    token.value = newToken
    username.value = newUsername
    localStorage.setItem('token', newToken)
    localStorage.setItem('username', newUsername)
    avatarUrl.value = newAvatarUrl || ''
    localStorage.setItem('avatarUrl', newAvatarUrl || '')
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  return { token, username, isLoggedIn, setLoginInfo, logout }
})