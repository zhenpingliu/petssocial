export interface UserInfo {
  id: string
  phone: string
  nickname: string
  avatar: string
  gender: number
  bio: string
  city: string
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: UserInfo
}

export interface LoginPayload {
  phone: string
  code: string
}

export interface RegisterPayload {
  phone: string
  code: string
  nickname: string
}

export interface RefreshPayload {
  refreshToken: string
}
