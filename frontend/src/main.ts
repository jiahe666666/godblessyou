import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

const authStore = useAuthStore(pinia)
authStore.hydrate()

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return '/auth'
  }
  if (to.meta.requiresAdmin && !authStore.admin) {
    return '/lottery'
  }
  return true
})

router.afterEach((to) => {
  document.title = `${String(to.meta.title ?? '抽奖系统')} - 抽奖系统`
})

app.use(router)
app.use(ElementPlus)
app.mount('#app')
