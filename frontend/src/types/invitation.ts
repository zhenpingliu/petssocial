import type { UserInfo } from './auth'
import type { Pet } from './pet'

export interface Invitation {
  id: string
  fromUserId: string
  toUserId: string
  fromUser: UserInfo
  toUser: UserInfo
  petId?: string
  pet?: Pet
  title: string
  content: string
  meetTime: string
  location: string
  status: 'pending' | 'accepted' | 'rejected' | 'cancelled'
  createdAt: string
}

export interface InvitationForm {
  toUserId: string
  petId?: string
  title: string
  content: string
  meetTime: string
  location: string
}
