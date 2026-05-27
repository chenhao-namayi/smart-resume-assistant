<template>
  <div class="history-page">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="$router.push('/dashboard')" :icon="ArrowLeft">返回</el-button>
          <h3>面试记录</h3>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="$router.push('/interview')">开始新面试</el-button>
        </div>
      </el-header>

      <el-main>
        <el-table :data="sessions" border stripe v-loading="loading" style="width: 100%; max-width: 1000px; margin: 0 auto;">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="resumeTitle" label="使用简历" width="180" show-overflow-tooltip />
          <el-table-column prop="position" label="意向岗位" width="150" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'primary'" size="small">
                {{ row.status === 'COMPLETED' ? '已完成' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="评分" width="70" />
          <el-table-column prop="questionCount" label="问题数" width="80" />
          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="row.status === 'COMPLETED'" text type="primary" size="small" @click="viewReport(row.id)">查看报告</el-button>
              <el-button v-else text type="warning" size="small" @click="continueInterview(row)">继续面试</el-button>
              <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && sessions.length === 0" description="还没有面试记录" style="margin-top: 80px;" />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInterviewHistory, deleteInterviewSession } from '../api/interview'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const sessions = ref([])
const loading = ref(false)

async function loadHistory() {
  loading.value = true
  try {
    const res = await getInterviewHistory()
    sessions.value = res.data
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function viewReport(id) { router.push(`/interview/report?id=${id}`) }

function continueInterview(row) {
  router.push(`/interview?sessionId=${row.id}`)
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除这条面试记录吗？', '确认删除', { type: 'warning' })
    await deleteInterviewSession(row.id)
    ElMessage.success('删除成功')
    await loadHistory()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

function fmt(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

onMounted(loadHistory)
</script>

<style scoped>
.history-page { min-height: 100vh; background: #f5f7fa; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left h3 { margin: 0; }
</style>
