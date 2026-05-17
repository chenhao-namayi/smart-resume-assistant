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
import { matchWithJob } from '../api/ai'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import MatchScore from '../components/MatchScore.vue'

const resumes = ref([])
const selectedResumeId = ref(null)
const jobDescription = ref('')
const matchResult = ref(null)
const matching = ref(false)

async function loadResumes() {
  try {
    const res = await getResumes()
    resumes.value = res.data
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
  } catch (e) {
    // handled by interceptor
  } finally {
    matching.value = false
  }
}

onMounted(loadResumes)
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
</style>
