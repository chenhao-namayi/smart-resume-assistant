<template>
  <div class="match-page">
    <el-container style="height: 100vh;">
      <el-header class="match-header">
        <div class="header-left">
          <el-button text @click="$router.push('/dashboard')" :icon="ArrowLeft">返回</el-button>
          <h3>岗位匹配分析</h3>
        </div>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <h4>选择简历</h4>
              <el-select v-model="selectedResumeId" placeholder="请选择简历" style="width: 100%;" @change="onResumeChange">
                <el-option v-for="r in resumes" :key="r.id" :label="r.title" :value="r.id" />
              </el-select>

              <h4 style="margin-top: 20px;">粘贴目标职位描述</h4>
              <el-input
                v-model="jobDescription"
                type="textarea"
                :rows="10"
                placeholder="请粘贴目标岗位的职位描述（JD）..."
              />
              <el-button type="warning" @click="handleMatch" :loading="matching" style="width: 100%; margin-top: 12px;">
                开始分析匹配度
              </el-button>
            </el-card>

            <el-card style="margin-top: 16px;" v-if="history.length > 0">
              <template #header>
                <span>匹配历史</span>
              </template>
              <div v-for="item in history" :key="item.id" class="history-item" @click="viewHistory(item)">
                <div class="history-main">
                  <span class="history-resume">{{ item.resumeTitle }}</span>
                  <span class="history-jd">{{ truncateText(item.jobDescription, 40) }}</span>
                </div>
                <div class="history-meta">
                  <el-tag :type="scoreTag(item.matchScore)" size="small">{{ item.matchScore }}分</el-tag>
                  <span class="history-time">{{ formatTime(item.createdAt) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <MatchScore :result="matchResult" v-if="matchResult" />
            <el-empty v-else description="请选择简历并粘贴JD，点击分析" />
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getResumes } from '../api/resume'
import { matchWithJob, getMatchHistory } from '../api/ai'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import MatchScore from '../components/MatchScore.vue'

const resumes = ref([])
const selectedResumeId = ref(null)
const jobDescription = ref('')
const matchResult = ref(null)
const matching = ref(false)
const history = ref([])

async function loadResumes() {
  try {
    const res = await getResumes()
    resumes.value = res.data
  } catch (e) {
    console.error(e)
  }
}

async function loadHistory() {
  try {
    const res = await getMatchHistory()
    history.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

function onResumeChange(val) {
  selectedResumeId.value = val
}

async function handleMatch() {
  if (!selectedResumeId.value) {
    ElMessage.warning('请先选择简历')
    return
  }
  if (!jobDescription.value.trim()) {
    ElMessage.warning('请输入职位描述')
    return
  }

  matching.value = true
  try {
    const res = await matchWithJob({
      resumeId: selectedResumeId.value,
      jobDescription: jobDescription.value
    })
    matchResult.value = res.data
    ElMessage.success('分析完成')
    await loadHistory()
  } catch (e) {
    // handled by interceptor
  } finally {
    matching.value = false
  }
}

function viewHistory(item) {
  matchResult.value = {
    score: item.matchScore,
    strengths: item.strengths,
    weaknesses: item.weaknesses,
    suggestions: item.suggestions
  }
  selectedResumeId.value = item.resumeId
  jobDescription.value = item.jobDescription
}

function truncateText(text, maxLen) {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

function scoreTag(score) {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

onMounted(() => {
  loadResumes()
  loadHistory()
})
</script>

<style scoped>
.match-page {
  background: #f5f7fa;
  min-height: 100vh;
}
.match-header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-left h3 {
  margin: 0;
}
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.15s;
}
.history-item:last-child {
  border-bottom: none;
}
.history-item:hover {
  background: #f5f7fa;
  margin: 0 -16px;
  padding-left: 16px;
  padding-right: 16px;
}
.history-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}
.history-resume {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}
.history-jd {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.history-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  margin-left: 12px;
}
.history-time {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
