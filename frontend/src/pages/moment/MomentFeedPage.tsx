import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  PullToRefresh,
  InfiniteScroll,
  Image,
  Button,
  Toast,
  Avatar,
} from 'antd-mobile'
import { HeartOutline, HeartFill, EditSOutline } from 'antd-mobile-icons'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { getMomentFeed, likeMoment, unlikeMoment } from '@/services/moment'
import type { Moment } from '@/types/moment'
import styles from './MomentFeedPage.module.css'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/** 根据图片数量返回对应的容器 className */
function getImageContainerClass(count: number): string {
  if (count === 1) return styles.images1
  if (count === 2) return styles.images2
  return styles.images3
}

export default function MomentFeedPage() {
  const navigate = useNavigate()
  const [moments, setMoments] = useState<Moment[]>([])
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const [total, setTotal] = useState(0)

  async function loadMore() {
    try {
      const res = await getMomentFeed(page, 10)
      if (page === 1) {
        setMoments(res.list)
      } else {
        setMoments((prev) => [...prev, ...res.list])
      }
      setTotal(res.total)
      setHasMore(moments.length + res.list.length < res.total)
      setPage((p) => p + 1)
    } catch (e: any) {
      Toast.show({ content: e?.message || '加载失败', icon: 'fail' })
      throw e
    }
  }

  async function handleRefresh() {
    setPage(1)
    setHasMore(true)
    try {
      const res = await getMomentFeed(1, 10)
      setMoments(res.list)
      setTotal(res.total)
      setHasMore(res.list.length < res.total)
      setPage(2)
    } catch (e: any) {
      Toast.show({ content: e?.message || '刷新失败', icon: 'fail' })
    }
  }

  async function toggleLike(moment: Moment) {
    const prev = moment.liked
    // 乐观更新
    setMoments((list) =>
      list.map((m) =>
        m.id === moment.id
          ? { ...m, liked: !prev, likeCount: m.likeCount + (prev ? -1 : 1) }
          : m,
      ),
    )
    try {
      if (prev) {
        await unlikeMoment(moment.id)
      } else {
        await likeMoment(moment.id)
      }
    } catch (e: any) {
      // 回滚
      setMoments((list) =>
        list.map((m) =>
          m.id === moment.id
            ? { ...m, liked: prev, likeCount: m.likeCount + (prev ? 1 : -1) }
            : m,
        ),
      )
      Toast.show({ content: e?.message || '操作失败', icon: 'fail' })
    }
  }

  return (
    <div className={styles.page}>
      {/* 发布按钮 */}
      <div className={styles.toolbar}>
        <Button
          color="primary"
          size="small"
          fill="outline"
          onClick={() => navigate('/moment/create')}
        >
          <EditSOutline /> 发布动态
        </Button>
      </div>

      <PullToRefresh onRefresh={handleRefresh}>
        <div className={styles.feed}>
          {moments.map((moment) => (
            <div key={moment.id} className={styles.card}>
              {/* 用户信息 */}
              <div className={styles.cardHeader}>
                <Avatar
                  src={moment.user?.avatar || ''}
                  style={{ '--size': '40px', '--border-radius': '50%' }}
                  fallback={<span className={styles.avatarFallback}>{moment.user?.nickname?.[0] || 'U'}</span>}
                />
                <div className={styles.userInfo}>
                  <span className={styles.nickname}>{moment.user?.nickname || '用户'}</span>
                  {moment.pet && (
                    <span className={styles.petTag}>🐾 {moment.pet.name}</span>
                  )}
                  <span className={styles.time}>{dayjs(moment.createdAt).fromNow()}</span>
                </div>
              </div>

              {/* 内容 */}
              <p className={styles.content}>{moment.content}</p>

              {/* 图片 */}
              {moment.images && moment.images.length > 0 && (
                <div className={getImageContainerClass(moment.images.length)}>
                  {moment.images.map((img, i) => (
                    <Image
                      key={i}
                      src={img}
                      fit="cover"
                      className={styles.image}
                    />
                  ))}
                </div>
              )}

              {/* 位置 */}
              {moment.location && (
                <p className={styles.location}>📍 {moment.location}</p>
              )}

              {/* 操作栏 */}
              <div className={styles.actions}>
                <button
                  className={`${styles.actionBtn} ${moment.liked ? styles.liked : ''}`}
                  onClick={() => toggleLike(moment)}
                >
                  {moment.liked ? <HeartFill /> : <HeartOutline />}
                  <span>{moment.likeCount}</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      </PullToRefresh>

      <InfiniteScroll loadMore={loadMore} hasMore={hasMore} />
    </div>
  )
}
