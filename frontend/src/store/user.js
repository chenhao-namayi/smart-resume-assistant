import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const role = ref(localStorage.getItem('role') || '')

  async function login(loginData) {
    const res = await apiLogin(loginData)
    token.value = res.data.token
    username.value = res.data.username
    userId.value = res.data.userId
    role.value = res.data.role || ''
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('role', res.data.role || '')
    return res
  }

  async function register(registerData) {
    const res = await apiRegister(registerData)
    token.value = res.data.token
    username.value = res.data.username
    userId.value = res.data.userId
    role.value = res.data.role || ''
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('role', res.data.role || '')
    return res
  }

  function logout() {
    token.value = ''
    username.value = ''
    userId.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userId')
    localStorage.removeItem('role')
  }

  const isAdmin = computed(() => role.value === 'ADMIN')

  return { token, username, userId, role, isAdmin, login, register, logout }
})
