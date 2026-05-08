import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { NavBar, TabBar } from 'antd-mobile'
import {
  AppstoreOutline,
  TeamOutline,
  HeartOutline,
  UserOutline,
} from 'antd-mobile-icons'
import styles from './MainLayout.module.css'

const tabs = [
  { key: '/pet', title: '宠物', icon: <AppstoreOutline /> },
  { key: '/moment', title: '动态', icon: <HeartOutline /> },
  { key: '/invitation', title: '邀约', icon: <TeamOutline /> },
  { key: '/profile', title: '我的', icon: <UserOutline /> },
]

export default function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()

  // 当前激活的 tab key
  const activeKey = tabs.find((t) => location.pathname.startsWith(t.key))?.key ?? '/moment'

  return (
    <div className={styles.layout}>
      <NavBar back={null} className={styles.navbar}>
        PetsSocial 🐾
      </NavBar>

      <div className={styles.content}>
        <Outlet />
      </div>

      <TabBar
        activeKey={activeKey}
        onChange={(key) => navigate(key)}
        className={styles.tabbar}
      >
        {tabs.map((tab) => (
          <TabBar.Item key={tab.key} icon={tab.icon} title={tab.title} />
        ))}
      </TabBar>
    </div>
  )
}
