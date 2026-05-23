<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useLotteryStore } from '../stores/lottery'

const authStore = useAuthStore()
const lotteryStore = useLotteryStore()
const drawing = ref(false)
const dialogVisible = ref(false)

const totalStock = computed(() => lotteryStore.prizes.reduce((sum, item) => sum + item.stockLeft, 0))
const enabledCount = computed(() => lotteryStore.prizes.filter((item) => item.enabled).length)

onMounted(async () => {
  await lotteryStore.fetchPrizes()
})

const handleDraw = async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录后再参与抽奖')
    return
  }
  if (!authStore.verified) {
    ElMessage.warning('请先完成邮箱验证')
    return
  }
  drawing.value = true
  try {
    await lotteryStore.draw()
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    drawing.value = false
  }
}
</script>

<template>
  <div class="stack-list">
    <div class="metric-grid">
      <el-card shadow="never" class="glass-card metric-card">
        <strong>{{ lotteryStore.prizes.length }}</strong>
        <span>奖池奖品数</span>
      </el-card>
      <el-card shadow="never" class="glass-card metric-card">
        <strong>{{ enabledCount }}</strong>
        <span>启用奖品数</span>
      </el-card>
      <el-card shadow="never" class="glass-card metric-card">
        <strong>{{ totalStock }}</strong>
        <span>总剩余库存</span>
      </el-card>
    </div>

    <el-card shadow="never" class="glass-card hero-block">
      <div class="card-header gap-between wrap-row">
        <div>
          <p class="section-kicker">抽奖大厅</p>
          <h3>面向用户的奖池展示与抽奖入口</h3>
          <p class="section-copy">已覆盖需求中的奖池列表、邮箱验证提醒、大抽奖按钮和中奖弹窗。</p>
        </div>
        <el-button type="primary" size="large" :loading="drawing" @click="handleDraw">立即抽奖</el-button>
      </div>
      <div class="stack-list compact-top">
        <el-alert :type="authStore.isLoggedIn ? 'success' : 'warning'" :closable="false" show-icon :title="authStore.isLoggedIn ? `已登录：${authStore.username}` : '请先登录后再抽奖'" />
        <el-alert :type="authStore.verified ? 'success' : 'warning'" :closable="false" show-icon :title="authStore.verified ? '邮箱已验证，可参与抽奖' : '邮箱未验证，当前仅可浏览奖池'" />
      </div>
    </el-card>

    <div class="page-grid prize-grid">
      <el-card v-for="prize in lotteryStore.prizes" :key="prize.id" shadow="never" class="glass-card prize-card">
        <template #header>
          <div class="card-header gap-between wrap-row">
            <strong>{{ prize.name }}</strong>
            <el-tag :type="prize.enabled ? 'success' : 'info'">{{ prize.enabled ? '启用中' : '已停用' }}</el-tag>
          </div>
        </template>
        <p class="section-copy">{{ prize.description }}</p>
        <div class="inline-meta">
          <span>类型：{{ prize.prizeType === 'code' ? '兑换码' : '图片' }}</span>
          <span>权重：{{ Math.round(prize.probability * 100) }}%</span>
        </div>
        <div class="inline-meta">
          <span>总库存：{{ prize.stockTotal }}</span>
          <span>剩余：{{ prize.stockLeft }}</span>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" title="中奖结果" width="560px">
      <div v-if="lotteryStore.latestResult" class="stack-list">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="奖品名称">{{ lotteryStore.latestResult.prizeName }}</el-descriptions-item>
          <el-descriptions-item label="奖品类型">{{ lotteryStore.latestResult.prizeType === 'code' ? '兑换码' : '图片' }}</el-descriptions-item>
          <el-descriptions-item label="发放内容">
            <template v-if="lotteryStore.latestResult.prizeType === 'image'">
              <el-image
                :src="lotteryStore.latestResult.content"
                fit="contain"
                :preview-src-list="[lotteryStore.latestResult.content]"
                style="width: 240px; max-width: 100%"
              />
            </template>
            <template v-else>
              {{ lotteryStore.latestResult.content }}
            </template>
          </el-descriptions-item>
          <el-descriptions-item label="剩余库存">{{ lotteryStore.latestResult.stockLeft }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>
