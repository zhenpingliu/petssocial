import { useState, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Form, Input, Button, Toast } from 'antd-mobile'
import { sendSmsCode, login } from '@/services/auth'
import { useAuthStore } from '@/stores/authStore'
import styles from './LoginPage.module.css'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const setAuth = useAuthStore((s) => s.setAuth)

  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [countdown, setCountdown] = useState(0)
  const [loading, setLoading] = useState(false)
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const from = (location.state as { from?: Location })?.from?.pathname || '/moment'

  function startCountdown() {
    setCountdown(60)
    timerRef.current = setInterval(() => {
      setCountdown((c) => {
        if (c <= 1) {
          clearInterval(timerRef.current!)
          return 0
        }
        return c - 1
      })
    }, 1000)
  }

  async function handleSendCode() {
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      Toast.show({ content: '请输入正确的手机号', icon: 'fail' })
      return
    }
    try {
      await sendSmsCode(phone)
      Toast.show({ content: '验证码已发送', icon: 'success' })
      startCountdown()
    } catch (e: any) {
      Toast.show({ content: e?.message || '发送失败', icon: 'fail' })
    }
  }

  async function handleLogin() {
    if (!phone || !code) {
      Toast.show({ content: '请填写完整信息', icon: 'fail' })
      return
    }
    setLoading(true)
    try {
      const res = await login({ phone, code })
      setAuth(res.user, res.accessToken, res.refreshToken)
      Toast.show({ content: '登录成功', icon: 'success' })
      navigate(from, { replace: true })
    } catch (e: any) {
      Toast.show({ content: e?.message || '登录失败', icon: 'fail' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <div className={styles.logo}>🐾</div>
        <h1 className={styles.title}>PetsSocial</h1>
        <p className={styles.subtitle}>宠物社交，从这里开始</p>
      </div>

      <div className={styles.form}>
        <Form layout="vertical">
          <Form.Item label="手机号">
            <Input
              placeholder="请输入手机号"
              type="tel"
              value={phone}
              onChange={setPhone}
              maxLength={11}
            />
          </Form.Item>

          <Form.Item label="验证码">
            <div className={styles.codeRow}>
              <Input
                placeholder="请输入验证码"
                type="number"
                value={code}
                onChange={setCode}
                maxLength={6}
              />
              <Button
                color="primary"
                fill="outline"
                size="small"
                disabled={countdown > 0}
                onClick={handleSendCode}
                className={styles.codeBtn}
              >
                {countdown > 0 ? `${countdown}s 后重试` : '获取验证码'}
              </Button>
            </div>
          </Form.Item>
        </Form>

        <Button
          block
          color="primary"
          size="large"
          loading={loading}
          onClick={handleLogin}
          className={styles.loginBtn}
        >
          登录 / 注册
        </Button>

        <p className={styles.tip}>未注册的手机号将自动创建账号</p>
      </div>
    </div>
  )
}
