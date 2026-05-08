import http from './http'
import type { Moment, MomentForm } from '@/types/moment'
import type { PageResult } from '@/types/common'
import type { UserInfo } from '@/types/auth'

const USE_MOCK = import.meta.env.DEV

const MOCK_USER: UserInfo = {
  id: 'mock-user-001',
  phone: '13800138000',
  nickname: '测试用户',
  avatar: '',
  gender: 0,
  bio: '',
  city: '北京',
  createdAt: '2024-01-01T00:00:00.000Z',
}

const MOCK_PET = {
  id: 'pet-001', userId: 'mock-user-001', name: '小白',
  species: 'dog', breed: '金毛', gender: 1,
  birthday: '2022-03-15', avatar: '', bio: '',
  createdAt: '2022-03-15T00:00:00.000Z',
}

const MOCK_MOMENTS: Moment[] = [
  {
    id: 'moment-001', userId: 'mock-user-001', user: MOCK_USER,
    petId: 'pet-001', pet: MOCK_PET,
    content: '今天带小白去公园玩，它可开心了！🐶',
    images: [
      'https://picsum.photos/seed/golden1/600/600',
      'https://picsum.photos/seed/golden2/600/600',
      'https://picsum.photos/seed/park3/600/600',
    ],
    location: '朝阳公园',
    likeCount: 3, commentCount: 1, liked: false,
    createdAt: new Date(Date.now() - 3_600_000).toISOString(),
  },
  {
    id: 'moment-002', userId: 'mock-user-001', user: MOCK_USER,
    petId: undefined, pet: undefined,
    content: '今天天气真好，适合带毛孩子出门～',
    images: [
      'https://picsum.photos/seed/sunny1/600/600',
      'https://picsum.photos/seed/sunny2/600/600',
    ],
    location: '望京',
    likeCount: 1, commentCount: 0, liked: true,
    createdAt: new Date(Date.now() - 86_400_000).toISOString(),
  },
  {
    id: 'moment-003', userId: 'mock-user-001', user: MOCK_USER,
    petId: 'pet-001', pet: MOCK_PET,
    content: '小白学会了握手！训练了整整一个月终于成功了 🎉',
    images: [
      'https://picsum.photos/seed/dogtrain/600/600',
    ],
    location: '家',
    likeCount: 15, commentCount: 5, liked: false,
    createdAt: new Date(Date.now() - 172_800_000).toISOString(),
  },
  {
    id: 'moment-004', userId: 'mock-user-001', user: MOCK_USER,
    petId: undefined, pet: undefined,
    content: '给小白买了一堆新玩具，它最喜欢那个球～',
    images: [
      'https://picsum.photos/seed/toys1/600/600',
      'https://picsum.photos/seed/toys2/600/600',
      'https://picsum.photos/seed/toys3/600/600',
      'https://picsum.photos/seed/toys4/600/600',
      'https://picsum.photos/seed/toys5/600/600',
      'https://picsum.photos/seed/toys6/600/600',
    ],
    location: '',
    likeCount: 8, commentCount: 2, liked: true,
    createdAt: new Date(Date.now() - 259_200_000).toISOString(),
  },
  {
    id: 'moment-005', userId: 'mock-user-001', user: MOCK_USER,
    petId: undefined, pet: undefined,
    content: '周末去宠物咖啡店，遇到了好多可爱的小猫🐱',
    images: [
      'https://picsum.photos/seed/catcafe1/600/600',
      'https://picsum.photos/seed/catcafe2/600/600',
      'https://picsum.photos/seed/catcafe3/600/600',
    ],
    location: '三里屯猫咪咖啡',
    likeCount: 22, commentCount: 7, liked: false,
    createdAt: new Date(Date.now() - 345_600_000).toISOString(),
  },
]

function genId() {
  return 'moment-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 8)
}

function paginate<T>(list: T[], page: number, pageSize: number): PageResult<T> {
  const start = (page - 1) * pageSize
  return { list: list.slice(start, start + pageSize), total: list.length, page, pageSize }
}

/** 获取动态 Feed 流 */
export function getMomentFeed(page = 1, pageSize = 10) {
  if (USE_MOCK) return Promise.resolve(paginate(MOCK_MOMENTS, page, pageSize))
  return http.get<PageResult<Moment>, PageResult<Moment>>('/moments/feed', {
    params: { page, pageSize },
  })
}

/** 发布动态 */
export function createMoment(data: MomentForm) {
  if (USE_MOCK) {
    const now = new Date().toISOString()
    const m: Moment = {
      id: genId(),
      userId: 'mock-user-001',
      user: MOCK_USER,
      petId: data.petId ?? undefined,
      pet: data.petId ? MOCK_PET : undefined,
      content: data.content,
      images: data.images ?? [],
      location: data.location ?? undefined,
      likeCount: 0,
      commentCount: 0,
      liked: false,
      createdAt: now,
    }
    MOCK_MOMENTS.unshift(m)
    return Promise.resolve(m)
  }
  return http.post<Moment, Moment>('/moments', data)
}

/** 点赞 */
export function likeMoment(id: string) {
  if (USE_MOCK) {
    const m = MOCK_MOMENTS.find((x) => x.id === id)
    if (m && !m.liked) { m.liked = true; m.likeCount++ }
    return Promise.resolve()
  }
  return http.post<void, void>(`/moments/${id}/like`)
}

/** 取消点赞 */
export function unlikeMoment(id: string) {
  if (USE_MOCK) {
    const m = MOCK_MOMENTS.find((x) => x.id === id)
    if (m && m.liked) { m.liked = false; m.likeCount-- }
    return Promise.resolve()
  }
  return http.delete<void, void>(`/moments/${id}/like`)
}
