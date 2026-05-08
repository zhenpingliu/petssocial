import { useState, useEffect } from 'react'
import {
  Tabs,
  List,
  Button,
  Popup,
  Form,
  Input,
  Toast,
  Empty,
  Tag,
  Dialog,
} from 'antd-mobile'
import dayjs from 'dayjs'
import {
  getSentInvitations,
  getReceivedInvitations,
  acceptInvitation,
  rejectInvitation,
  cancelInvitation,
} from '@/services/invitation'
import type { Invitation } from '@/types/invitation'
import styles from './InvitationPage.module.css'

const statusConfig: Record<
  Invitation['status'],
  { label: string; color: 'default' | 'success' | 'danger' | 'warning' | 'primary' }
> = {
  pending: { label: '待处理', color: 'warning' },
  accepted: { label: '已接受', color: 'success' },
  rejected: { label: '已拒绝', color: 'danger' },
  cancelled: { label: '已取消', color: 'default' },
}

export default function InvitationPage() {
  const [activeTab, setActiveTab] = useState('received')
  const [received, setReceived] = useState<Invitation[]>([])
  const [sent, setSent] = useState<Invitation[]>([])
  const [loading, setLoading] = useState(false)

  async function loadReceived() {
    setLoading(true)
    try {
      const res = await getReceivedInvitations(1, 20)
      setReceived(res.list)
    } catch (e: any) {
      Toast.show({ content: e?.message || '加载失败', icon: 'fail' })
    } finally {
      setLoading(false)
    }
  }

  async function loadSent() {
    setLoading(true)
    try {
      const res = await getSentInvitations(1, 20)
      setSent(res.list)
    } catch (e: any) {
      Toast.show({ content: e?.message || '加载失败', icon: 'fail' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadReceived()
    loadSent()
  }, [])

  async function handleAccept(inv: Invitation) {
    try {
      const updated = await acceptInvitation(inv.id)
      setReceived((prev) => prev.map((i) => (i.id === updated.id ? updated : i)))
      Toast.show({ content: '已接受邀约', icon: 'success' })
    } catch (e: any) {
      Toast.show({ content: e?.message || '操作失败', icon: 'fail' })
    }
  }

  async function handleReject(inv: Invitation) {
    const confirmed = await Dialog.confirm({
      content: '确定拒绝这个邀约吗？',
      confirmText: '拒绝',
      cancelText: '再想想',
    })
    if (!confirmed) return
    try {
      const updated = await rejectInvitation(inv.id)
      setReceived((prev) => prev.map((i) => (i.id === updated.id ? updated : i)))
      Toast.show({ content: '已拒绝', icon: 'success' })
    } catch (e: any) {
      Toast.show({ content: e?.message || '操作失败', icon: 'fail' })
    }
  }

  async function handleCancel(inv: Invitation) {
    const confirmed = await Dialog.confirm({
      content: '确定取消这个邀约吗？',
      confirmText: '取消邀约',
      cancelText: '不了',
    })
    if (!confirmed) return
    try {
      const updated = await cancelInvitation(inv.id)
      setSent((prev) => prev.map((i) => (i.id === updated.id ? updated : i)))
      Toast.show({ content: '邀约已取消', icon: 'success' })
    } catch (e: any) {
      Toast.show({ content: e?.message || '操作失败', icon: 'fail' })
    }
  }

  function renderInvitation(inv: Invitation, mode: 'received' | 'sent') {
    const statusCfg = statusConfig[inv.status]
    return (
      <div key={inv.id} className={styles.card}>
        <div className={styles.cardTop}>
          <div className={styles.cardTitle}>{inv.title}</div>
          <Tag color={statusCfg.color}>{statusCfg.label}</Tag>
        </div>

        <p className={styles.cardContent}>{inv.content}</p>

        <div className={styles.cardMeta}>
          <span>🕐 {dayjs(inv.meetTime).format('MM-DD HH:mm')}</span>
          <span>📍 {inv.location}</span>
        </div>

        {mode === 'received' && (
          <div className={styles.cardFrom}>
            来自：{inv.fromUser?.nickname || '未知用户'}
            {inv.pet && ` · 🐾 ${inv.pet.name}`}
          </div>
        )}

        {mode === 'sent' && (
          <div className={styles.cardFrom}>
            发给：{inv.toUser?.nickname || '未知用户'}
          </div>
        )}

        {inv.status === 'pending' && (
          <div className={styles.cardActions}>
            {mode === 'received' ? (
              <>
                <Button size="small" color="success" onClick={() => handleAccept(inv)}>
                  接受
                </Button>
                <Button size="small" color="danger" fill="outline" onClick={() => handleReject(inv)}>
                  拒绝
                </Button>
              </>
            ) : (
              <Button size="small" fill="outline" onClick={() => handleCancel(inv)}>
                取消
              </Button>
            )}
          </div>
        )}
      </div>
    )
  }

  return (
    <div className={styles.page}>
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <Tabs.Tab title="收到的邀约" key="received">
          {received.length === 0 ? (
            <Empty description="暂无邀约" imageStyle={{ width: 100 }} />
          ) : (
            <div className={styles.list}>
              {received.map((inv) => renderInvitation(inv, 'received'))}
            </div>
          )}
        </Tabs.Tab>

        <Tabs.Tab title="发出的邀约" key="sent">
          {sent.length === 0 ? (
            <Empty description="暂未发出邀约" imageStyle={{ width: 100 }} />
          ) : (
            <div className={styles.list}>
              {sent.map((inv) => renderInvitation(inv, 'sent'))}
            </div>
          )}
        </Tabs.Tab>
      </Tabs>
    </div>
  )
}
