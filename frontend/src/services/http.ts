import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { tokenStore } from '@/utils/token'
import type { Result } from '@/types/common'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器：添加 Authorization Token
http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStore.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 是否正在刷新 token
let isRefreshing = false
// 等待刷新的请求队列
let waitingQueue: Array<(token: string) => void> = []

function processQueue(token: string) {
  waitingQueue.forEach((cb) => cb(token))
  waitingQueue = []
}

// 响应拦截器：自动解包 Result<T>，401 自动刷新 token
http.interceptors.response.use(
  (response) => {
    const result = response.data as Result
    if (result.code === 0 || result.code === 200) {
      return result.data as any
    }
    return Promise.reject(new Error(result.message || '请求失败'))
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = tokenStore.getRefreshToken()
      if (!refreshToken) {
        tokenStore.clearTokens()
        window.location.href = '/login'
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return new Promise((resolve) => {
          waitingQueue.push((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(http(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const res = await axios.post<Result<{ accessToken: string; refreshToken: string }>>(
          '/api/auth/refresh',
          { refreshToken },
        )
        const { accessToken, refreshToken: newRefreshToken } = res.data.data
        tokenStore.setTokens(accessToken, newRefreshToken)
        processQueue(accessToken)
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return http(originalRequest)
      } catch {
        tokenStore.clearTokens()
        window.location.href = '/login'
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    const message =
      (error.response?.data as Result)?.message ||
      error.message ||
      '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export default http
