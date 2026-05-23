import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 8000,
})

let refreshingPromise: Promise<string | null> | null = null

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('lottery-access-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const refreshToken = localStorage.getItem('lottery-refresh-token')
    const requestUrl = String(originalRequest?.url ?? '')
    const isAuthEndpoint = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/refresh')

    if (error.response?.status === 401 && refreshToken && !isAuthEndpoint && !originalRequest?._retry) {
      originalRequest._retry = true
      if (!refreshingPromise) {
        refreshingPromise = axios
          .post(`${http.defaults.baseURL}/auth/refresh`, { refreshToken })
          .then((response) => {
            const tokens = response.data.data
            localStorage.setItem('lottery-access-token', tokens.accessToken)
            localStorage.setItem('lottery-refresh-token', tokens.refreshToken)
            localStorage.setItem(
              'lottery-profile',
              JSON.stringify({
                username: tokens.username,
                verified: tokens.verified,
                admin: tokens.admin,
              }),
            )
            return tokens.accessToken as string
          })
          .catch(() => null)
          .finally(() => {
            refreshingPromise = null
          })
      }

      const newAccessToken = await refreshingPromise
      if (newAccessToken) {
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return http(originalRequest)
      }
    }

    if (error.response?.status === 401) {
      localStorage.removeItem('lottery-access-token')
      localStorage.removeItem('lottery-refresh-token')
      localStorage.removeItem('lottery-profile')
      if (window.location.pathname !== '/auth') {
        window.location.href = '/auth'
      }
    }
    return Promise.reject(error)
  },
)

export default http
