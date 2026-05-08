import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  List,
  Button,
  Popup,
  Form,
  Input,
  Dialog,
  Toast,
  Avatar,
} from 'antd-mobile'
import { EditSOutline, RightOutline } from 'antd-mobile-icons'
import { updateProfile } from '@/services/user'
import { logout } from '@/services/auth'
import { useAuthStore } from '@/stores/authStore'
import styles from './ProfilePage.module.css'

export default function ProfilePage() {
  const navigate = useNavigate()
  const { user, setUser, clearAuth } = useAuthStore()

  const [editVisible, setEditVisible] = useState(false)
  const [nickname, setNickname] = useState(user?.nickname || '')
  const [bio, setBio] = useState(user?.bio || '')
  const [saving, setSaving] = useState(false)

  async function handleSave() {
    setSaving(true)
    try {
      const updated = await updateProfile({ nickname: nickname.trim(), bio: bio.trim() })
      setUser(updated)
      Toast.show({ content: '保存成功', icon: 'success' })
      setEditVisible(false)
    } catch (e: any) {
      Toast.show({ content: e?.message || '保存失败', icon: 'fail' })
    } finally {
      setSaving(false)
    }
  }

  async function handleLogout() {
    const confirmed = await Dialog.confirm({
      content: '确定退出登录吗？',
      confirmText: '退出',
      cancelText: '取消',
    })
    if (!confirmed) return
    try {
      await logout()
    } catch {}
    clearAuth()
    navigate('/login', { replace: true })
  }

  return (
    <div className={styles.page}>
      {/* 头部个人信息 */}
      <div className={styles.header}>
        <Avatar
          src={user?.avatar || ''}
          style={{ '--size': '80px', '--border-radius': '50%' }}
          fallback={
            <div className={styles.avatarFallback}>
              {user?.nickname?.[0] || 'U'}
            </div>
          }
        />
        <div className={styles.userInfo}>
          <div className={styles.nickname}>{user?.nickname || '未设置昵称'}</div>
          <div className={styles.phone}>📱 {user?.phone || ''}</div>
          {user?.bio && <div className={styles.bio}>{user.bio}</div>}
        </div>
        <Button
          fill="none"
          className={styles.editBtn}
          onClick={() => {
            setNickname(user?.nickname || '')
            setBio(user?.bio || '')
            setEditVisible(true)
          }}
        >
          <EditSOutline fontSize={20} />
        </Button>
      </div>

      {/* 统计 */}
      {/* 功能列表 */}
      <List className={styles.list}>
        <List.Item prefix="🐾" arrow={<RightOutline />}>
          我的宠物
        </List.Item>
        <List.Item prefix="📝" arrow={<RightOutline />}>
          我的动态
        </List.Item>
        <List.Item prefix="💌" arrow={<RightOutline />} onClick={() => navigate('/invitation')}>
          我的邀约
        </List.Item>
      </List>

      <div className={styles.logoutWrap}>
        <Button block color="danger" fill="outline" onClick={handleLogout}>
          退出登录
        </Button>
      </div>

      {/* 编辑 Popup */}
      <Popup
        visible={editVisible}
        onMaskClick={() => setEditVisible(false)}
        position="bottom"
        bodyStyle={{ borderRadius: '16px 16px 0 0', padding: '20px 0' }}
      >
        <div className={styles.popupHeader}>
          <span className={styles.popupTitle}>编辑资料</span>
          <span className={styles.popupClose} onClick={() => setEditVisible(false)}>✕</span>
        </div>

        <Form layout="vertical" style={{ padding: '0 16px' }}>
          <Form.Item label="昵称">
            <Input
              placeholder="请输入昵称"
              value={nickname}
              onChange={setNickname}
              maxLength={20}
            />
          </Form.Item>

          <Form.Item label="个人简介">
            <Input
              placeholder="介绍一下自己（选填）"
              value={bio}
              onChange={setBio}
              maxLength={100}
            />
          </Form.Item>

          <Button
            block
            color="primary"
            loading={saving}
            onClick={handleSave}
            style={{ marginTop: 12, borderRadius: 10 }}
          >
            保存
          </Button>
        </Form>
      </Popup>
    </div>
  )
}
