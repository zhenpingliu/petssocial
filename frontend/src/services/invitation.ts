import http from './http'
import type { Invitation, InvitationForm } from '@/types/invitation'
import type { PageResult } from '@/types/common'
import type { UserInfo } from '@/types/auth'

const USE_MOCK = import.meta.env.DEV

const MOCK_USER: UserInfo = {
  id: 'mock-user-001', phone: '13800138000', nickname: '测试用户',
  avatar: '', gender: 0, bio: '', city: '北京', createdAt: '2024-01-01T00:00:00.000Z',
}
const MOCK_FRIEND: UserInfo = {
  id: 'user-002', phone: '13800138001', nickname: '小伙伴',
  avatar: '', gender: 0, bio: '', city: '北京', createdAt: '2024-01-01T00:00:00.000Z',
}
const MOCK_PET = {
  id: 'pet-001', userId: 'mock-user-001', name: '小白', species: 'dog',
  breed: '金毛', gender: 1, birthday: '2022-03-15', avatar: '', bio: '', createdAt: '2022-03-15T00:00:00.000Z',
}

const MOCK_SENT: Invitation[] = [
  {
    id: 'inv-001', fromUserId: 'mock-user-001', toUserId: 'user-002',
    fromUser: MOCK_USER, toUser: MOCK_FRIEND,
    petId: 'pet-001', pet: MOCK_PET,
    title: '周末遛狗', content: '一起带毛孩子出来玩吧！',
    meetTime: new Date(Date.now() + 86400_000).toISOString(),
    location: '朝阳公园', status: 'pending',
    createdAt: new Date(Date.now() - 3600_000).toISOString(),
  },
]

const MOCK_RECEIVED: Invitation[] = [
  {
    id: 'inv-002', fromUserId: 'user-003', toUserId: 'mock-user-001',
    fromUser: { ...MOCK_FRIEND, id: 'user-003', nickname: '邻居小王' },
    toUser: MOCK_USER,
    petId: undefined, pet: undefined,
    title: '猫咪聚会', content: '我家猫想和你家猫玩',
    meetTime: new Date(Date.now() + 172800_000).toISOString(),
    location: '奥林匹克森林公园', status: 'pending',
    createdAt: new Date(Date.now() - 1800_000).toISOString(),
  },
]

function genId() {
  return 'inv-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 8)
}

function paginate<T>(list: T[], page: number, pageSize: number): PageResult<T> {
  const start = (page - 1) * pageSize
  return { list: list.slice(start, start + pageSize), total: list.length, page, pageSize }
}

/** 发起邀约 */
export function createInvitation(data: InvitationForm) {
  if (USE_MOCK) {
    const now = new Date().toISOString()
    const inv: Invitation = {
      id: genId(),
      fromUserId: 'mock-user-001', toUserId: data.toUserId,
      fromUser: MOCK_USER, toUser: MOCK_FRIEND,
      petId: data.petId ?? undefined, pet: data.petId ? MOCK_PET : undefined,
      title: data.title, content: data.content,
      meetTime: data.meetTime, location: data.location,
      status: 'pending', createdAt: now,
    }
    MOCK_SENT.push(inv)
    return Promise.resolve(inv)
  }
  return http.post<Invitation, Invitation>('/invitations', data)
}

/** 获取已发出的邀约 */
export function getSentInvitations(page = 1, pageSize = 10) {
  if (USE_MOCK) return Promise.resolve(paginate(MOCK_SENT, page, pageSize))
  return http.get<PageResult<Invitation>, PageResult<Invitation>>('/invitations/sent', {
    params: { page, pageSize },
  })
}

/** 获取收到的邀约 */
export function getReceivedInvitations(page = 1, pageSize = 10) {
  if (USE_MOCK) return Promise.resolve(paginate(MOCK_RECEIVED, page, pageSize))
  return http.get<PageResult<Invitation>, PageResult<Invitation>>('/invitations/received', {
    params: { page, pageSize },
  })
}

/** 接受邀约 */
export function acceptInvitation(id: string) {
  if (USE_MOCK) {
    const inv = MOCK_RECEIVED.find((x) => x.id === id)
    if (inv) inv.status = 'accepted'
    return Promise.resolve(inv!)
  }
  return http.put<Invitation, Invitation>(`/invitations/${id}/accept`)
}

/** 拒绝邀约 */
export function rejectInvitation(id: string) {
  if (USE_MOCK) {
    const inv = MOCK_RECEIVED.find((x) => x.id === id)
    if (inv) inv.status = 'rejected'
    return Promise.resolve(inv!)
  }
  return http.put<Invitation, Invitation>(`/invitations/${id}/reject`)
}

/** 取消邀约（发起方） */
export function cancelInvitation(id: string) {
  if (USE_MOCK) {
    const inv = MOCK_SENT.find((x) => x.id === id)
    if (inv) inv.status = 'cancelled'
    return Promise.resolve(inv!)
  }
  return http.put<Invitation, Invitation>(`/invitations/${id}/cancel`)
}
