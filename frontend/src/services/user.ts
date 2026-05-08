import http from './http'
import type { UserInfo } from '@/types/auth'
import { useAuthStore } from '@/stores/authStore'

const USE_MOCK = import.meta.env.DEV

let mockUserInfo: UserInfo = {
  id: 'mock-user-001',
  phone: '13800138000',
  nickname: '测试用户',
  avatar: '',
  gender: 0,
  bio: '这是一个模拟账号，用于前端开发调试',
  city: '北京',
  createdAt: '2024-01-01T00:00:00.000Z',
}

/** 获取当前用户信息 */
export function getProfile() {
  if (USE_MOCK) {
    const authUser = useAuthStore.getState().user
    if (authUser?.phone) mockUserInfo.phone = authUser.phone
    return Promise.resolve({ ...mockUserInfo })
  }
  return http.get<UserInfo, UserInfo>('/user/profile')
}

/** 更新用户信息 */
export function updateProfile(
  data: Partial<Pick<UserInfo, 'nickname' | 'avatar' | 'bio' | 'city' | 'gender'>>,
) {
  if (USE_MOCK) {
    Object.assign(mockUserInfo, data)
    return Promise.resolve({ ...mockUserInfo })
  }
  return http.put<UserInfo, UserInfo>('/user/profile', data)
}
