import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const avatarUrl = ref(localStorage.getItem('avatarUrl') || '')
  const userId = ref(localStorage.getItem('userId') || '')

  const isLoggedIn = () => {
    return token.value !== ''
  }

  const setLoginInfo = (newToken, newUsername, newAvatarUrl, newUserId) => {
    token.value = newToken
    username.value = newUsername
    avatarUrl.value = newAvatarUrl || ''
    userId.value = newUserId || ''
    localStorage.setItem('token', newToken)
    localStorage.setItem('username', newUsername)
    localStorage.setItem('avatarUrl', newAvatarUrl || '')
    localStorage.setItem('userId', newUserId || '')
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    avatarUrl.value = ''
    userId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('avatarUrl')
    localStorage.removeItem('userId')
  }

  return { token, username, avatarUrl, userId, isLoggedIn, setLoginInfo, logout }
})