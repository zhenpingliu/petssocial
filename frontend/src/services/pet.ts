import http from './http'
import type { Pet, PetForm } from '@/types/pet'

const USE_MOCK = import.meta.env.DEV

const MOCK_USER_ID = 'mock-user-001'

const MOCK_PETS: Pet[] = [
  {
    id: 'pet-001', userId: MOCK_USER_ID,
    name: '小白', species: 'dog', breed: '金毛寻回犬',
    gender: 1, birthday: '2022-03-15',
    avatar: 'https://picsum.photos/seed/goldendog/400/400',
    bio: '活泼好动',
    createdAt: '2024-01-01T00:00:00.000Z',
  },
  {
    id: 'pet-002', userId: MOCK_USER_ID,
    name: '小黑', species: 'cat', breed: '英国短毛猫',
    gender: 2, birthday: '2023-06-20',
    avatar: 'https://picsum.photos/seed/britishcat/400/400',
    bio: '高冷女王',
    createdAt: '2024-02-15T00:00:00.000Z',
  },
  {
    id: 'pet-003', userId: MOCK_USER_ID,
    name: '旺财', species: 'dog', breed: '柯基',
    gender: 1, birthday: '2023-01-10',
    avatar: 'https://picsum.photos/seed/corgidog/400/400',
    bio: '小短腿，大能量',
    createdAt: '2024-03-20T00:00:00.000Z',
  },
]

function genId() {
  return 'pet-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 8)
}

function fillPet(form: PetForm, id: string, createdAt: string): Pet {
  return {
    id,
    userId: MOCK_USER_ID,
    name: form.name,
    species: form.species,
    breed: form.breed,
    gender: form.gender,
    birthday: form.birthday,
    avatar: form.avatar ?? '',
    bio: form.bio ?? '',
    createdAt,
  }
}

/** 获取宠物列表（当前用户） */
export function getPetList() {
  if (USE_MOCK) return Promise.resolve(MOCK_PETS.map((p) => ({ ...p })))
  return http.get<Pet[], Pet[]>('/pets')
}

/** 创建宠物 */
export function createPet(data: PetForm) {
  if (USE_MOCK) {
    const now = new Date().toISOString()
    const pet = fillPet(data, genId(), now)
    MOCK_PETS.push(pet)
    return Promise.resolve({ ...pet })
  }
  return http.post<Pet, Pet>('/pets', data)
}

/** 更新宠物信息 */
export function updatePet(id: string, data: Partial<PetForm>) {
  if (USE_MOCK) {
    const idx = MOCK_PETS.findIndex((p) => p.id === id)
    if (idx === -1) return Promise.reject(new Error('宠物不存在'))
    const p = MOCK_PETS[idx]
    MOCK_PETS[idx] = {
      ...p,
      ...data,
      avatar: data.avatar ?? p.avatar,
      bio: data.bio ?? p.bio,
    }
    return Promise.resolve({ ...MOCK_PETS[idx] })
  }
  return http.put<Pet, Pet>(`/pets/${id}`, data)
}

/** 删除宠物 */
export function deletePet(id: string) {
  if (USE_MOCK) {
    const idx = MOCK_PETS.findIndex((p) => p.id === id)
    if (idx !== -1) MOCK_PETS.splice(idx, 1)
    return Promise.resolve()
  }
  return http.delete<void, void>(`/pets/${id}`)
}
