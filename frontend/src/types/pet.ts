export interface Pet {
  id: string
  userId: string
  name: string
  species: string   // 物种：cat / dog / other
  breed: string     // 品种
  gender: number    // 0=未知 1=公 2=母
  birthday: string
  avatar: string
  bio: string
  createdAt: string
}

export interface PetForm {
  name: string
  species: string
  breed: string
  gender: number
  birthday: string
  avatar?: string
  bio?: string
}
