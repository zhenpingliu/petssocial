import http from './http'

const USE_MOCK = import.meta.env.DEV

/** 最大图片数量 */
export const MAX_IMAGES = 9

/** 最大图片宽度（超过会压缩） */
const MAX_WIDTH = 1200
/** 压缩质量 */
const QUALITY = 0.8

/**
 * 压缩图片：将大图缩放到 MAX_WIDTH 以内
 */
function compressImage(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    // 小图直接返回 base64
    if (file.size < 100 * 1024) {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result as string)
      reader.onerror = reject
      reader.readAsDataURL(file)
      return
    }

    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      let { width, height } = img

      if (width > MAX_WIDTH) {
        height = Math.round((height * MAX_WIDTH) / width)
        width = MAX_WIDTH
      }

      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')!
      ctx.drawImage(img, 0, 0, width, height)
      resolve(canvas.toDataURL('image/jpeg', QUALITY))
    }
    img.onerror = reject
    img.src = URL.createObjectURL(file)
  })
}

/**
 * 上传单张图片，返回图片 URL
 * - mock 模式：压缩后返回 base64 data URL
 * - 正式模式：POST /api/upload/image 返回服务端 URL
 */
export async function uploadImage(file: File): Promise<string> {
  const base64 = await compressImage(file)

  if (USE_MOCK) {
    // mock 模式直接返回 base64
    return base64
  }

  // 正式模式：上传 base64 到后端
  const res = await http.post<{ url: string }, { url: string }>(
    '/upload/image',
    { image: base64 },
  )
  return res.url
}
