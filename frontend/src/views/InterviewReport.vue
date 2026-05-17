<template>
  <div class="report-page">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="$router.push('/interview/history')" :icon="ArrowLeft">返回记录</el-button>
          <h3>面试报告</h3>
        </div>
      </el-header>

      <el-main v-loading="loading">
        <div class="report-container" v-if="report">
          <div class="score-section">
            <div class="score-circle" :class="scoreLevel">
              <span class="score-num">{{ report.score || 0 }}</span>
              <span class="score-label">综合评分</span>
            </div>
            <div class="score-meta">
              <p>简历：{{ report.resumeTitle }}</p>
              <p>问题数：{{ report.totalQuestions }}</p>
              <p>完成时间：{{ fmt(report.completedAt) }}</p>
            </div>
          </div>

          <el-divider />

          <div class="section">
            <h4>综合评语</h4>
            <p class="report-text">{{ report.report }}</p>
          </div>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="section">
                <h4 style="color: #67C23A;">优势</h4>
                <ul>
                  <li v-for="(s, i) in report.strengths" :key="i">{{ s }}</li>
                </ul>
                <el-empty v-if="!report.strengths?.length" description="暂无" :image-size="40" />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="section">
                <h4 style="color: #E6A23C;">不足</h4>
                <ul>
                  <li v-for="(w, i) in report.weaknesses" :key="i">{{ w }}</li>
                </ul>
                <el-empty v-if="!report.weaknesses?.length" description="暂无" :image-size="40" />
              </div>
            </el-col>
          </el-row>

          <div class="section">
            <h4 style="color: #409EFF;">改进建议</h4>
            <ol>
              <li v-for="(s, i) in report.suggestions" :key="i">{{ s }}</li>
            </ol>
            <el-empty v-if="!report.suggestions?.length" description="暂无" :image-size="40" />
          </div>
        </div>
        <el-empty v-else description="报告不存在或已删除" />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSessionDetail } from '../api/interview'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const report = ref(null)
const loading = ref(false)

const scoreLevel = computed(() => {
  const s = report.value?.score || 0
  if (s >= 80) return 'high'
  if (s >= 60) return 'mid'
  return 'low'
})

async function loadReport() {
  loading.value = true
  try {
    const res = await getSessionDetail(route.query.id)
    const session = res.data
    report.value = {
      sessionId: session.id,
      resumeId: session.resume?.id,
      resumeTitle: session.resume?.title || '未知',
      score: session.score,
      totalQuestions: countQuestions(session.messages),
      report: session.report,
      strengths: parseList(session.strengths),
      weaknesses: parseList(session.weaknesses),
      suggestions: parseList(session.suggestions),
      completedAt: session.completedAt
    }
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function countQuestions(messages) {
  try {
    const msgs = typeof messages === 'string' ? JSON.parse(messages) : messages
    return msgs?.filter(m => m.role === 'ai').length || 0
  } catch { return 0 }
}

function parseList(val) {
  if (!val) return []
  try { return JSON.parse(val) } catch { return val ? [val] : [] }
}

function fmt(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

onMounted(loadReport)
</script>

<style scoped>
.report-page { min-height: 100vh; background: #f5f7fa; }
.header {
  display: flex; align-items: center; background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left h3 { margin: 0; }
.report-container { max-width: 800px; margin: 0 auto; }
.score-section {
  display: flex; align-items: center; gap: 30px; padding: 20px 0;
}
.score-circle {
  width: 120px; height: 120px; border-radius: 50%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  border: 4px solid #909399;
}
.score-circle.high { border-color: #67C23A; background: #f0f9eb; }
.score-circle.mid { border-color: #E6A23C; background: #fdf6ec; }
.score-circle.low { border-color: #F56C6C; background: #fef0f0; }
.score-num { font-size: 36px; font-weight: bold; line-height: 1; }
.score-label { font-size: 12px; color: #909399; margin-top: 4px; }
.score-meta p { margin: 4px 0; color: #606266; font-size: 14px; }
.section { background: #fff; padding: 16px 20px; border-radius: 8px; margin-bottom: 16px; }
.section h4 { margin: 0 0 8px; font-size: 15px; }
.report-text { line-height: 1.8; color: #303133; white-space: pre-wrap; }
.section ul, .section ol { margin: 0; padding-left: 20px; }
.section li { line-height: 1.8; color: #606266; }
</style>
