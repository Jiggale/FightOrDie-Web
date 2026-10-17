import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// JWT 自动登录：本地无 token 时调用登录接口获取，存 localStorage
export async function ensureToken(): Promise<string> {
  const existing = localStorage.getItem('fod_token')
  if (existing) return existing
  const resp = await axios.post('/api/auth/login', {
    username: 'hero' + Math.floor(Math.random() * 9000 + 1000),
    password: '123456',
  })
  const token = (resp.data as { data: string }).data
  localStorage.setItem('fod_token', token)
  return token
}

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('fod_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && body.code === 200) {
      return body.data
    }
    console.error('业务错误:', body?.message)
    return Promise.reject(new Error(body?.message || '请求失败'))
  },
  (error) => {
    console.error('网络错误:', error)
    return Promise.reject(error)
  },
)

export default request
