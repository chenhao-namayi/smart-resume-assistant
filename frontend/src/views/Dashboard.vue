<template>
  <div class="dashboard">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h2>AI Resume Copilot</h2>
        </div>
        <div class="header-right">
          <el-button v-if="userStore.isAdmin" type="warning" plain size="small" @click="$router.push('/admin')">管理后台</el-button>
          <span class="username">{{ userStore.username }}</span>
          <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <div class="toolbar">
          <h3>我的简历</h3>
          <el-button type="primary" @click="showCreateDialog = true" :icon="Plus">新建简历</el-button>
        </div>

        <el-row :gutter="20" v-if="resumes.length > 0">
          <el-col :span="8" v-for="item in resumes" :key="item.id" style="margin-bottom: 20px;">
            <el-card class="resume-card" shadow="hover" @click="openEditor(item.id)">
              <div class="card-content">
                <h4>{{ item.title }}</h4>
                <p class="version">V{{ item.version }}</p>
                <p class="meta">更新于 {{ formatDate(item.updatedAt) }}</p>
                <el-tag v-if="item.isCurrent" type="success" size="small">当前版本</el-tag>
              </div>
              <div class="card-actions">
                <el-button text type="primary" size="small" @click.stop="openEditor(item.id)">编辑</el-button>
                <el-button text type="danger" size="small" @click.stop="handleDelete(item.id)">删除</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-else description="还没有简历，点击上方按钮新建" />

        <!-- Match button -->
        <div class="match-section" v-if="resumes.length > 0">
          <el-divider />
          <h3>岗位匹配分析</h3>
          <p class="desc">将你的简历与目标岗位进行匹配度分析</p>
          <el-button type="warning" @click="$router.push('/match')" :icon="TrendCharts">去分析</el-button>
        </div>

        <!-- Interview section -->
        <div class="interview-section" v-if="resumes.length > 0">
          <el-divider />
          <h3>AI模拟面试</h3>
          <p class="desc">AI面试官基于简历进行模拟面试，面试结束后生成评估报告</p>
          <div class="interview-actions">
            <el-button type="success" @click="$router.push('/interview')" :icon="ChatDotRound">开始面试</el-button>
            <el-button @click="$router.push('/interview/history')" :icon="List">面试记录</el-button>
          </div>
        </div>
      </el-main>
    </el-container>

    <!-- Create Dialog -->
    <el-dialog v-model="showCreateDialog" title="新建简历" width="700px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef">
        <el-form-item label="简历标题" prop="title">
          <el-input v-model="createForm.title" placeholder="例如：后端开发工程师简历" />
        </el-form-item>
        <el-form-item label="选择模板">
          <div class="template-grid">
            <div
              v-for="tpl in templates"
              :key="tpl.id"
              class="template-card"
              :class="{ selected: createForm.templateId === tpl.id }"
              @click="createForm.templateId = tpl.id; createForm.contentJson = tpl.contentJson"
            >
              <div class="tpl-category">
                <el-tag size="small" :type="categoryType(tpl.category)">{{ tpl.category }}</el-tag>
              </div>
              <h4>{{ tpl.name }}</h4>
              <p class="tpl-desc">{{ tpl.description }}</p>
              <div class="tpl-check" v-if="createForm.templateId === tpl.id">
                <el-icon color="#409EFF" :size="20"><CircleCheckFilled /></el-icon>
              </div>
            </div>
          </div>
          <div class="no-template">
            <el-button text type="primary" @click="createForm.templateId = null; createForm.contentJson = null">
              不使用模板，从空白开始
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getResumes, createResume, deleteResume } from '../api/resume'
import { getTemplates } from '../api/template'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, TrendCharts, CircleCheckFilled, ChatDotRound, List } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const resumes = ref([])
const templates = ref([])
const showCreateDialog = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = ref({ title: '', templateId: null, contentJson: null })
const createRules = {
  title: [{ required: true, message: '请输入简历标题', trigger: 'blur' }]
}

async function loadTemplates() {
  try {
    const res = await getTemplates()
    templates.value = res.data || []
  } catch (e) {
    console.error('Failed to load templates', e)
  }
}

async function loadResumes() {
  try {
    const res = await getResumes()
    resumes.value = res.data
  } catch (e) {
    console.error('Failed to load resumes', e)
  }
}

function openEditor(id) {
  router.push(`/editor/${id}`)
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这份简历吗？', '确认删除')
    await deleteResume(id)
    ElMessage.success('删除成功')
    await loadResumes()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleCreate() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    const payload = { title: createForm.value.title }
    if (createForm.value.contentJson) {
      payload.contentJson = createForm.value.contentJson
    }
    const res = await createResume(payload)
    createForm.value = { title: '', templateId: null, contentJson: null }
    showCreateDialog.value = false
    ElMessage.success('创建成功')
    router.push(`/editor/${res.data.id}`)
  } catch (e) {
    // handled by interceptor
  } finally {
    creating.value = false
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

function categoryType(category) {
  const map = { '技术开发': '', '产品设计': 'success', '市场运营': 'warning', '应届生': 'info', '管理': 'danger' }
  return map[category] || 'info'
}

onMounted(() => {
  loadResumes()
  loadTemplates()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-left h2 {
  margin: 0;
  font-size: 18px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  color: #606266;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.resume-card {
  cursor: pointer;
}
.card-content h4 {
  margin: 0 0 8px;
}
.version {
  color: #909399;
  font-size: 13px;
}
.meta {
  color: #c0c4cc;
  font-size: 12px;
  margin: 4px 0;
}
.card-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
.match-section {
  margin-top: 20px;
}
.desc {
  color: #909399;
  font-size: 14px;
  margin: 8px 0;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  max-height: 360px;
  overflow-y: auto;
  padding: 4px;
}
.template-card {
  border: 2px solid #ebeef5;
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}
.template-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}
.template-card.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.template-card h4 {
  margin: 8px 0 4px;
  font-size: 14px;
}
.tpl-category {
  margin-bottom: 4px;
}
.tpl-desc {
  color: #909399;
  font-size: 12px;
  margin: 0;
  line-height: 1.4;
}
.tpl-check {
  position: absolute;
  top: 8px;
  right: 8px;
}
.no-template {
  text-align: center;
  margin-top: 8px;
}
</style>
