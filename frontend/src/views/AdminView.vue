<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useLotteryStore } from '../stores/lottery'
import type { Prize, PrizeCreatePayload } from '../types'

const lotteryStore = useLotteryStore()
const dialogVisible = ref(false)
const editDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const editingPrize = ref<Prize | null>(null)

const form = reactive<PrizeCreatePayload>({
  name: '',
  description: '',
  prizeType: 'code',
  probability: 0.1,
  contents: '',
  enabled: true,
})

const editForm = reactive<PrizeCreatePayload>({
  name: '',
  description: '',
  prizeType: 'code',
  probability: 0.1,
  contents: '',
  enabled: true,
})

onMounted(async () => {
  await lotteryStore.fetchDashboard()
  await lotteryStore.fetchAdminPrizes()
})

const handleCreate = async () => {
  await lotteryStore.createPrize(form)
  ElMessage.success('奖品已创建')
  dialogVisible.value = false
  form.name = ''
  form.description = ''
  form.prizeType = 'code'
  form.probability = 0.1
  form.contents = ''
  form.enabled = true
}

const openEdit = (prize: Prize) => {
  editingPrize.value = prize
  editForm.name = prize.name
  editForm.description = prize.description
  editForm.prizeType = prize.prizeType
  editForm.probability = prize.probability
  editForm.enabled = prize.enabled
  editForm.contents = ''
  editDialogVisible.value = true
}

const handleEdit = async () => {
  if (!editingPrize.value) return
  await lotteryStore.updatePrize(editingPrize.value.id, editForm)
  ElMessage.success('奖品已更新')
  editDialogVisible.value = false
}

const handleToggle = async (id: number) => {
  await lotteryStore.togglePrize(id)
  ElMessage.success('奖品状态已切换')
}

const handleDelete = async (prize: Prize) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除奖品「${prize.name}」吗？此操作不可恢复。`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await lotteryStore.deletePrize(prize.id)
    ElMessage.success('奖品已删除')
  } catch {
    // cancelled
  }
}

const openAuditLog = async () => {
  await lotteryStore.fetchStockAudits()
  auditDialogVisible.value = true
}
</script>

<template>
  <div class="stack-list">
    <div class="metric-grid admin-metric-grid">
      <el-card shadow="never" class="glass-card metric-card">
        <strong>{{ lotteryStore.dashboard.prizeCount }}</strong>
        <span>奖品总数</span>
      </el-card>
      <el-card shadow="never" class="glass-card metric-card">
        <strong>{{ lotteryStore.dashboard.enabledPrizeCount }}</strong>
        <span>启用奖品</span>
      </el-card>
      <el-card shadow="never" class="glass-card metric-card">
        <strong>{{ lotteryStore.dashboard.stockLeftTotal }}</strong>
        <span>剩余库存</span>
      </el-card>
      <el-card shadow="never" class="glass-card metric-card">
        <strong>{{ lotteryStore.dashboard.winnerCount }}</strong>
        <span>中奖记录</span>
      </el-card>
    </div>

    <el-card shadow="never" class="glass-card">
      <template #header>
        <div class="card-header gap-between wrap-row">
          <div>
            <p class="section-kicker">奖品管理</p>
            <h3>后台 CRUD 入口</h3>
          </div>
          <div class="inline-meta">
            <el-button @click="openAuditLog">库存审计日志</el-button>
            <el-button type="primary" @click="dialogVisible = true">新增奖品</el-button>
          </div>
        </div>
      </template>

      <el-table :data="lotteryStore.adminPrizes" stripe>
        <el-table-column prop="name" label="奖品名称" min-width="160" />
        <el-table-column label="类型" min-width="100">
          <template #default="scope">
            {{ scope.row.prizeType === 'code' ? '兑换码' : '图片' }}
          </template>
        </el-table-column>
        <el-table-column prop="probability" label="权重" min-width="100" />
        <el-table-column prop="stockLeft" label="剩余库存" min-width="100" />
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="260">
          <template #default="scope">
            <el-button type="primary" link @click="openEdit(scope.row)">编辑</el-button>
            <el-button type="primary" link @click="handleToggle(scope.row.id)">切换状态</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增奖品弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增奖品" width="620px">
      <el-form label-position="top" :model="form">
        <el-form-item label="奖品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="奖品描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <div class="page-grid two-columns compact-grid">
          <el-form-item label="奖品类型">
            <el-select v-model="form.prizeType" class="full-width">
              <el-option label="兑换码" value="code" />
              <el-option label="图片" value="image" />
            </el-select>
          </el-form-item>
          <el-form-item label="概率权重">
            <el-input-number v-model="form.probability" :min="0.01" :max="1" :step="0.01" class="full-width" />
          </el-form-item>
        </div>
        <el-form-item label="库存内容（每行一个）">
          <el-input v-model="form.contents" type="textarea" :rows="6" placeholder="CODE-001&#10;CODE-002" />
        </el-form-item>
        <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑奖品弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑奖品" width="620px">
      <el-form label-position="top" :model="editForm">
        <el-form-item label="奖品名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="奖品描述">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <div class="page-grid two-columns compact-grid">
          <el-form-item label="奖品类型">
            <el-select v-model="editForm.prizeType" class="full-width">
              <el-option label="兑换码" value="code" />
              <el-option label="图片" value="image" />
            </el-select>
          </el-form-item>
          <el-form-item label="概率权重">
            <el-input-number v-model="editForm.probability" :min="0.01" :max="1" :step="0.01" class="full-width" />
          </el-form-item>
        </div>
        <el-form-item label="库存内容（仅新增时生效，编辑不改库存）">
          <el-input v-model="editForm.contents" type="textarea" :rows="4" placeholder="编辑奖品时不修改库存" disabled />
        </el-form-item>
        <el-switch v-model="editForm.enabled" active-text="启用" inactive-text="禁用" />
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 库存审计日志弹窗 -->
    <el-dialog v-model="auditDialogVisible" title="库存审计日志" width="800px">
      <el-table :data="lotteryStore.stockAudits" stripe max-height="500">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="prizeName" label="奖品名称" min-width="120" />
        <el-table-column prop="changeType" label="变更类型" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.changeType === 'LOTTERY_DRAW' ? 'warning' : 'info'" size="small">
              {{ scope.row.changeType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="delta" label="变动" width="80">
          <template #default="scope">
            <span :style="{ color: scope.row.delta > 0 ? '#67c23a' : scope.row.delta < 0 ? '#f56c6c' : '#909399' }">
              {{ scope.row.delta > 0 ? '+' : '' }}{{ scope.row.delta }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="stockBefore" label="变动前" width="80" />
        <el-table-column prop="stockAfter" label="变动后" width="80" />
        <el-table-column prop="remark" label="备注" min-width="160" />
        <el-table-column prop="createdAt" label="时间" min-width="160" />
      </el-table>
      <template #footer>
        <el-button @click="auditDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
