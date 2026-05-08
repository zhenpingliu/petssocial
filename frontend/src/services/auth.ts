import http from './http'
import type { AuthResponse, LoginPayload, RegisterPayload } from '@/types/auth'

// 开发模式下使用模拟登录（无需真实短信/后端）
const USE_MOCK = import.meta.env.DEV

const MOCK_USER_BASE = {
  id: 'mock-user-001',
  nickname: '测试用户',
  avatar: '',
  gender: 0,
  bio: '这是一个模拟账号，用于前端开发调试',
  city: '北京',
  createdAt: '2024-01-01T00:00:00.000Z',
}

function makeMockAuth(phone: string): AuthResponse {
  return {
    accessToken: `mock-access-${Date.now()}`,
    refreshToken: `mock-refresh-${Date.now()}`,
    user: { ...MOCK_USER_BASE, phone },
  }
}

/** 发送短信验证码（模拟模式：直接成功，控制台打印验证码） */
export function sendSmsCode(phone: string) {
  if (USE_MOCK) {
    console.log(`[MOCK] 短信验证码已发送 → 手机号：${phone}，验证码：123456`)
    return Promise.resolve()
  }
  return http.post<void, void>('/auth/sms', { phone })
}

/** 登录（手机号 + 验证码），模拟模式接受任意验证码 */
export function login(payload: LoginPayload) {
  if (USE_MOCK) {
    console.log(`[MOCK] 登录成功 → 手机号：${payload.phone}`)
    return Promise.resolve(makeMockAuth(payload.phone))
  }
  return http.post<AuthResponse, AuthResponse>('/auth/login', payload)
}

/** 注册（模拟模式自动创建账号） */
export function register(payload: RegisterPayload) {
  if (USE_MOCK) {
    console.log(`[MOCK] 注册成功 → 手机号：${payload.phone}`)
    return Promise.resolve(makeMockAuth(payload.phone))
  }
  return http.post<AuthResponse, AuthResponse>('/auth/register', payload)
}

/** 退出登录 */
export function logout() {
  if (USE_MOCK) {
    console.log('[MOCK] 已退出登录')
    return Promise.resolve()
  }
  return http.post<void, void>('/auth/logout')
}
