import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { NavBar, TextArea, Button, Selector, Toast } from 'antd-mobile'
import { CloseOutline, AddCircleOutline } from 'antd-mobile-icons'
import { getPetList } from '@/services/pet'
import { createMoment } from '@/services/moment'
import { uploadImage, MAX_IMAGES } from '@/services/upload'
import type { Pet } from '@/types/pet'
import styles from './MomentCreatePage.module.css'

export default function MomentCreatePage() {
  const navigate = useNavigate()
  const [content, setContent] = useState('')
  const [location, setLocation] = useState('')
  const [petId, setPetId] = useState<string | undefined>()
  const [pets, setPets] = useState<Pet[]>([])
  const [images, setImages] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    getPetList().then(setPets).catch(() => {})
  }, [])

  const petOptions = [
    { label: '不关联宠物', value: '' },
    ...pets.map((p) => ({ label: p.name, value: p.id })),
  ]

  function handleSelectFiles() {
    const remaining = MAX_IMAGES - images.length
    if (remaining <= 0) {
      Toast.show({ content: `最多上传 ${MAX_IMAGES} 张图片`, icon: 'fail' })
      return
    }
    fileInputRef.current?.click()
  }

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = e.target.files
    if (!files || files.length === 0) return

    const remaining = MAX_IMAGES - images.length
    const toProcess = Array.from(files).slice(0, remaining)

    Toast.show({ icon: 'loading', content: '图片处理中...', duration: 0 })

    try {
      const results = await Promise.all(toProcess.map((f) => uploadImage(f)))
      setImages((prev) => [...prev, ...results])
    } catch {
      Toast.show({ content: '图片处理失败', icon: 'fail' })
    } finally {
      // 重置 input 以允许重复选择同一文件
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
      Toast.clear()
    }
  }

  function removeImage(index: number) {
    setImages((prev) => prev.filter((_, i) => i !== index))
  }

  async function handleSubmit() {
    if (!content.trim() && images.length === 0) {
      Toast.show({ content: '请填写动态内容或添加图片', icon: 'fail' })
      return
    }
    setSubmitting(true)
    try {
      await createMoment({
        content: content.trim(),
        location: location.trim() || undefined,
        petId: petId || undefined,
        images: images.length > 0 ? images : undefined,
      })
      Toast.show({ content: '发布成功！', icon: 'success' })
      navigate(-1)
    } catch (e: any) {
      Toast.show({ content: e?.message || '发布失败', icon: 'fail' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className={styles.page}>
      <NavBar onBack={() => navigate(-1)} right={
        <Button
          color="primary"
          size="small"
          loading={submitting}
          onClick={handleSubmit}
        >
          发布
        </Button>
      }>
        发布动态
      </NavBar>

      <div className={styles.body}>
        <TextArea
          placeholder="分享你和宠物的有趣故事..."
          value={content}
          onChange={setContent}
          rows={6}
          maxLength={500}
          showCount
          className={styles.textarea}
        />

        {/* 图片选择区域 */}
        <div className={styles.section}>
          <div className={styles.imageGrid}>
            {images.map((img, i) => (
              <div key={i} className={styles.imageItem}>
                <img src={img} alt="" className={styles.imageThumb} />
                <button
                  className={styles.imageRemove}
                  onClick={() => removeImage(i)}
                  type="button"
                >
                  <CloseOutline fontSize={14} />
                </button>
              </div>
            ))}
            {images.length < MAX_IMAGES && (
              <button
                className={styles.addImageBtn}
                onClick={handleSelectFiles}
                type="button"
              >
                <AddCircleOutline fontSize={32} />
                <span className={styles.addImageText}>
                  {images.length}/{MAX_IMAGES}
                </span>
              </button>
            )}
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            style={{ display: 'none' }}
          />
        </div>

        <div className={styles.section}>
          <div className={styles.sectionLabel}>📍 添加位置</div>
          <input
            type="text"
            placeholder="输入位置（选填）"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            className={styles.locationInput}
          />
        </div>

        {pets.length > 0 && (
          <div className={styles.section}>
            <div className={styles.sectionLabel}>🐾 关联宠物</div>
            <Selector
              options={petOptions}
              value={[petId ?? '']}
              onChange={(v) => setPetId(v[0] || undefined)}
            />
          </div>
        )}
      </div>
    </div>
  )
}
