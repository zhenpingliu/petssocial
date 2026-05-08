import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserInfo } from '@/types/auth'
import { tokenStore } from '@/utils/token'

interface AuthState {
  user: UserInfo | null
  isAuthed: boolean
  setAuth: (user: UserInfo, accessToken: string, refreshToken: string) => void
  clearAuth: () => void
  setUser: (user: UserInfo) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthed: false,

      setAuth(user, accessToken, refreshToken) {
        tokenStore.setTokens(accessToken, refreshToken)
        set({ user, isAuthed: true })
      },

      clearAuth() {
        tokenStore.clearTokens()
        set({ user: null, isAuthed: false })
      },

      setUser(user) {
        set({ user })
      },
    }),
    {
      name: 'ps-auth',
      partialize: (state) => ({ user: state.user, isAuthed: state.isAuthed }),
    },
  ),
)
