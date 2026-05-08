import { useState, useEffect, useRef } from 'react'
import {
  Button,
  Popup,
  Form,
  Input,
  Selector,
  Dialog,
  Toast,
  Empty,
  FloatingBubble,
  Image,
} from 'antd-mobile'
import { AddOutline, EditSOutline, DeleteOutline, CloseOutline } from 'antd-mobile-icons'
import dayjs from 'dayjs'
import { getPetList, createPet, updatePet, deletePet } from '@/services/pet'
import { uploadImage } from '@/services/upload'
import type { Pet, PetForm } from '@/types/pet'
import styles from './PetPage.module.css'

const speciesOptions = [
  { label: '🐱 猫咪', value: 'cat' },
  { label: '🐶 狗狗', value: 'dog' },
  { label: '🐰 其他', value: 'other' },
]

const genderOptions = [
  { label: '公', value: 1 },
  { label: '母', value: 2 },
  { label: '未知', value: 0 },
]

const defaultForm: PetForm = {
  name: '',
  species: 'cat',
  breed: '',
  gender: 0,
  birthday: '',
  bio: '',
}

/** 物种对应 emoji */
function speciesEmoji(species: string) {
  if (species === 'cat') return '🐱'
  if (species === 'dog') return '🐶'
  return '🐰'
}

export default function PetPage() {
  const [pets, setPets] = useState<Pet[]>([])
  const [loading, setLoading] = useState(false)
  const [popupVisible, setPopupVisible] = useState(false)
  const [editingPet, setEditingPet] = useState<Pet | null>(null)
  const [form, setForm] = useState<PetForm>(defaultForm)
  const [avatarPreview, setAvatarPreview] = useState<string>('')
  const [submitting, setSubmitting] = useState(false)
  const [avatarUploading, setAvatarUploading] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  async function fetchPets() {
    setLoading(true)
    try {
      const data = await getPetList()
      setPets(data)
    } catch (e: any) {
      Toast.show({ content: e?.message || '加载失败', icon: 'fail' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPets()
  }, [])

  function openCreate() {
    setEditingPet(null)
    setForm(defaultForm)
    setAvatarPreview('')
    setPopupVisible(true)
  }

  function openEdit(pet: Pet) {
    setEditingPet(pet)
    setForm({
      name: pet.name,
      species: pet.species,
      breed: pet.breed,
      gender: pet.gender,
      birthday: pet.birthday,
      bio: pet.bio,
      avatar: pet.avatar,
    })
    setAvatarPreview(pet.avatar)
    setPopupVisible(true)
  }

  async function handleAvatarSelect(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    setAvatarUploading(true)
    try {
      const url = await uploadImage(file)
      setAvatarPreview(url)
      setForm((f) => ({ ...f, avatar: url }))
    } catch {
      Toast.show({ content: '图片上传失败', icon: 'fail' })
    } finally {
      setAvatarUploading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  function removeAvatar() {
    setAvatarPreview('')
    setForm((f) => ({ ...f, avatar: '' }))
  }

  async function handleSubmit() {
    if (!form.name.trim()) {
      Toast.show({ content: '请输入宠物名称', icon: 'fail' })
      return
    }
    setSubmitting(true)
    try {
      const payload = {
        ...form,
        avatar: avatarPreview || undefined,
      }
      if (editingPet) {
        const updated = await updatePet(editingPet.id, payload)
        setPets((prev) => prev.map((p) => (p.id === updated.id ? updated : p)))
        Toast.show({ content: '修改成功', icon: 'success' })
      } else {
        const created = await createPet(payload)
        setPets((prev) => [...prev, created])
        Toast.show({ content: '添加成功', icon: 'success' })
      }
      setPopupVisible(false)
    } catch (e: any) {
      Toast.show({ content: e?.message || '操作失败', icon: 'fail' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(pet: Pet) {
    const confirmed = await Dialog.confirm({
      content: `确定要删除 ${pet.name} 吗？`,
      confirmText: '删除',
      cancelText: '取消',
    })
    if (!confirmed) return
    try {
      await deletePet(pet.id)
      setPets((prev) => prev.filter((p) => p.id !== pet.id))
      Toast.show({ content: '已删除', icon: 'success' })
    } catch (e: any) {
      Toast.show({ content: e?.message || '删除失败', icon: 'fail' })
    }
  }

  return (
    <div className={styles.page}>
      {pets.length === 0 && !loading ? (
        <Empty description="还没有宠物，快添加一只吧~" imageStyle={{ width: 120 }} />
      ) : (
        <div className={styles.grid}>
          {pets.map((pet) => (
            <div key={pet.id} className={styles.card}>
              {/* 宠物头像/封面 */}
              <div className={styles.cardCover}>
                {pet.avatar ? (
                  <Image src={pet.avatar} fit="cover" className={styles.cardImage} />
                ) : (
                  <div className={styles.cardPlaceholder}>
                    {speciesEmoji(pet.species)}
                  </div>
                )}
                {/* 操作按钮 */}
                <div className={styles.cardActions}>
                  <button className={styles.cardActionBtn} onClick={() => openEdit(pet)}>
                    <EditSOutline fontSize={18} />
                  </button>
                  <button className={styles.cardActionBtn} onClick={() => handleDelete(pet)}>
                    <DeleteOutline fontSize={18} />
                  </button>
                </div>
                {/* 物种标签 */}
                <span className={styles.speciesTag}>
                  {speciesEmoji(pet.species)} {pet.breed || '未知品种'}
                </span>
              </div>

              {/* 宠物信息 */}
              <div className={styles.cardInfo}>
                <div className={styles.cardName}>{pet.name}</div>
                <div className={styles.cardMeta}>
                  <span>{pet.gender === 1 ? '♂ 公' : pet.gender === 2 ? '♀ 母' : '未知'}</span>
                  <span>·</span>
                  <span>
                    {pet.birthday
                      ? dayjs().diff(dayjs(pet.birthday), 'month') + ' 个月'
                      : '年龄未知'}
                  </span>
                </div>
                {pet.bio && <div className={styles.cardBio}>{pet.bio}</div>}
              </div>
            </div>
          ))}
        </div>
      )}

      <FloatingBubble
        style={{ '--initial-position-bottom': '80px', '--initial-position-right': '24px' }}
        onClick={openCreate}
      >
        <AddOutline fontSize={28} />
      </FloatingBubble>

      <Popup
        visible={popupVisible}
        onMaskClick={() => setPopupVisible(false)}
        position="bottom"
        bodyStyle={{ borderRadius: '16px 16px 0 0', padding: '20px 0' }}
      >
        <div className={styles.popupHeader}>
          <span className={styles.popupTitle}>{editingPet ? '编辑宠物' : '添加宠物'}</span>
          <span className={styles.popupClose} onClick={() => setPopupVisible(false)}>✕</span>
        </div>

        <Form layout="vertical" style={{ padding: '0 16px' }}>
          {/* 头像选择 */}
          <Form.Item label="头像">
            <div className={styles.avatarPicker}>
              {avatarPreview ? (
                <div className={styles.avatarPreviewWrap}>
                  <Image src={avatarPreview} fit="cover" className={styles.avatarPreview} />
                  <button className={styles.avatarRemove} onClick={removeAvatar} type="button">
                    <CloseOutline fontSize={14} />
                  </button>
                </div>
              ) : (
                <button className={styles.avatarAddBtn} onClick={() => fileInputRef.current?.click()} type="button">
                  <AddOutline fontSize={24} />
                  <span>选择头像</span>
                </button>
              )}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleAvatarSelect}
                style={{ display: 'none' }}
              />
            </div>
          </Form.Item>

          <Form.Item label="名字" required>
            <Input
              placeholder="宠物的名字"
              value={form.name}
              onChange={(v) => setForm((f) => ({ ...f, name: v }))}
            />
          </Form.Item>

          <Form.Item label="物种">
            <Selector
              options={speciesOptions}
              value={[form.species]}
              onChange={(v) => setForm((f) => ({ ...f, species: v[0] ?? 'cat' }))}
            />
          </Form.Item>

          <Form.Item label="品种">
            <Input
              placeholder="如：英短、柯基"
              value={form.breed}
              onChange={(v) => setForm((f) => ({ ...f, breed: v }))}
            />
          </Form.Item>

          <Form.Item label="性别">
            <Selector
              options={genderOptions}
              value={[form.gender]}
              onChange={(v) => setForm((f) => ({ ...f, gender: v[0] ?? 0 }))}
            />
          </Form.Item>

          <Form.Item label="生日（YYYY-MM-DD）">
            <Input
              placeholder="如：2022-06-15"
              value={form.birthday}
              onChange={(v) => setForm((f) => ({ ...f, birthday: v }))}
            />
          </Form.Item>

          <Form.Item label="简介">
            <Input
              placeholder="介绍一下你的宠物"
              value={form.bio}
              onChange={(v) => setForm((f) => ({ ...f, bio: v }))}
            />
          </Form.Item>

          <Button
            block
            color="primary"
            loading={submitting || avatarUploading}
            onClick={handleSubmit}
            style={{ marginTop: 12, borderRadius: 10 }}
          >
            {editingPet ? '保存修改' : '添加宠物'}
          </Button>
        </Form>
      </Popup>
    </div>
  )
}
