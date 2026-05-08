import { Routes, Route, Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import MainLayout from '@/layouts/MainLayout'
import LoginPage from '@/pages/auth/LoginPage'
import PetPage from '@/pages/pet/PetPage'
import MomentFeedPage from '@/pages/moment/MomentFeedPage'
import MomentCreatePage from '@/pages/moment/MomentCreatePage'
import InvitationPage from '@/pages/invitation/InvitationPage'
import ProfilePage from '@/pages/profile/ProfilePage'

function RequireAuth() {
  const isAuthed = useAuthStore((s) => s.isAuthed)
  const location = useLocation()

  if (!isAuthed) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <Outlet />
}

export default function AppRouter() {
  return (
    <Routes>
      {/* 公开路由 */}
      <Route path="/login" element={<LoginPage />} />

      {/* 受保护路由 */}
      <Route element={<RequireAuth />}>
        <Route element={<MainLayout />}>
          <Route index element={<Navigate to="/moment" replace />} />
          <Route path="/pet" element={<PetPage />} />
          <Route path="/moment" element={<MomentFeedPage />} />
          <Route path="/moment/create" element={<MomentCreatePage />} />
          <Route path="/invitation" element={<InvitationPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
      </Route>

      {/* 兜底重定向 */}
      <Route path="*" element={<Navigate to="/moment" replace />} />
    </Routes>
  )
}
