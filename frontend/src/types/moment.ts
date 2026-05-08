import type { UserInfo } from './auth'
import type { Pet } from './pet'

export interface Moment {
  id: string
  userId: string
  user: UserInfo
  petId?: string
  pet?: Pet
  content: string
  images: string[]
  location?: string
  likeCount: number
  commentCount: number
  liked: boolean
  createdAt: string
}

export interface MomentForm {
  petId?: string
  content: string
  images?: string[]
  location?: string
}
