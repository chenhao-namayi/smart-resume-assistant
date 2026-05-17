<template>
  <div class="editor-page">
    <el-container style="height: 100vh;">
      <el-header class="editor-header">
        <div class="header-left">
          <el-button text @click="$router.push('/dashboard')" :icon="ArrowLeft">返回</el-button>
          <h3>{{ resume?.title || '加载中...' }}</h3>
          <el-tag v-if="resume?.version" size="small">V{{ resume.version }}</el-tag>
        </div>
        <div class="header-actions">
          <el-button @click="handleSave" :loading="saving" type="primary" :icon="Check">保存</el-button>
          <el-button @click="handleCreateVersion" :icon="CopyDocument">创建新版本</el-button>
          <el-button @click="handleExport" :icon="Download">导出PDF</el-button>
        </div>
      </el-header>
      <el-main class="editor-main">
        <el-row :gutter="16" style="height: 100%;">
          <el-col :span="14" style="height: 100%;">
            <el-card class="form-card" style="height: 100%; overflow-y: auto;">
              <ResumeForm ref="resumeFormRef" :resume-data="resumeData" @update="handleFormUpdate" @ai-optimize="handleAIOptimize" />
            </el-card>
          </el-col>
          <el-col :span="10" style="height: 100%;">
            <AIPanel ref="aiPanelRef" :resume-id="resumeId" :resume-data="resumeData" @apply-optimization="handleApplyOptimization" />
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getResume, updateResume, createVersion } from '../api/resume'
import { exportToPDF } from '../utils/pdfExport'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, CopyDocument, Download } from '@element-plus/icons-vue'
import ResumeForm from '../components/ResumeForm.vue'
import AIPanel from '../components/AIPanel.vue'

const route = useRoute()
const resumeId = ref(parseInt(route.params.resumeId))

const resume = ref(null)
const resumeData = ref(null)
const resumeFormRef = ref(null)
const aiPanelRef = ref(null)
const saving = ref(false)

async function loadResume() {
  try {
    const res = await getResume(resumeId.value)
    resume.value = res.data
    resumeData.value = res.data.contentJson ? JSON.parse(res.data.contentJson) : getDefaultResumeData()
  } catch (e) {
    ElMessage.error('加载简历失败')
  }
}

function getDefaultResumeData() {
  return {
    basicInfo: { name: '', phone: '', email: '', position: '' },
    workExperience: [],
    education: [],
    skills: '',
    projects: [],
    summary: ''
  }
}

function handleFormUpdate(data) {
  resumeData.value = data
}

function handleAIOptimize({ section, index, text }) {
  if (aiPanelRef.value) {
    aiPanelRef.value.setSection(section, index, text)
  }
}

function handleApplyOptimization({ section, index, text }) {
  if (!resumeData.value) return
  if (index !== undefined && Array.isArray(resumeData.value[section])) {
    resumeData.value[section][index].description = text
  } else if (typeof resumeData.value[section] === 'string') {
    resumeData.value[section] = text
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateResume(resumeId.value, {
      contentJson: JSON.stringify(resumeData.value)
    })
    ElMessage.success('保存成功')
    await loadResume()
  } catch (e) {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}

async function handleCreateVersion() {
  try {
    await ElMessageBox.confirm('将基于当前内容创建新版本，继续吗？', '创建版本')
    await createVersion(resumeId.value)
    ElMessage.success('新版本已创建')
    await loadResume()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleExport() {
  if (!resumeData.value) {
    ElMessage.warning('没有可导出的简历内容')
    return
  }
  await exportToPDF(resumeData.value, `${resume.value?.title || 'resume'}.pdf`)
  ElMessage.success('导出成功')
}

onMounted(loadResume)
</script>

<style scoped>
.editor-page {
  background: #f5f7fa;
}
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-left h3 {
  margin: 0;
  font-size: 16px;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.editor-main {
  height: calc(100vh - 60px);
  padding: 12px;
}
.form-card {
  border-radius: 8px;
}
</style>
