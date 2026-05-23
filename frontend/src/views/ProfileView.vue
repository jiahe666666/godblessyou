<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useLotteryStore } from '../stores/lottery'

const lotteryStore = useLotteryStore()

onMounted(async () => {
  await lotteryStore.fetchHistory()
})

const handleCopy = async (content: string) => {
  await navigator.clipboard.writeText(content)
  ElMessage.success('已复制到剪贴板')
}
</script>

<template>
  <el-card shadow="never" class="glass-card">
    <template #header>
      <div class="card-header">
        <div>
          <p class="section-kicker">个人中心</p>
          <h3>中奖记录</h3>
        </div>
      </div>
    </template>

    <el-table :data="lotteryStore.history" stripe>
      <el-table-column prop="prizeName" label="奖品名称" min-width="160" />
      <el-table-column label="奖品类型" min-width="120">
        <template #default="scope">
          {{ scope.row.prizeType === 'code' ? '兑换码' : '图片' }}
        </template>
      </el-table-column>
      <el-table-column label="发放内容" min-width="260">
        <template #default="scope">
          <div v-if="scope.row.prizeType === 'image'" class="stack-list">
            <el-image
              :src="scope.row.content"
              fit="cover"
              :preview-src-list="[scope.row.content]"
              style="width: 120px; height: 120px; border-radius: 12px"
            />
            <span>{{ scope.row.content }}</span>
          </div>
          <div v-else class="inline-meta">
            <span>{{ scope.row.content }}</span>
            <el-button type="primary" link @click="handleCopy(scope.row.content)">
              复制
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="中奖时间" min-width="180" />
    </el-table>
  </el-card>
</template>
