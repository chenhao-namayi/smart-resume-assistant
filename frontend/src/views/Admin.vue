<template>
  <div class="admin-page">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h2>管理后台</h2>
        </div>
        <div class="header-right">
          <span class="username">{{ userStore.username }}</span>
          <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main>
        <el-tabs v-model="activeTab" type="border-card" @tab-change="onTabChange">
          <!-- ==================== 统计概览 ==================== -->
          <el-tab-pane label="统计概览" name="stats">
            <el-row :gutter="20" v-loading="loadingStats">
              <el-col :span="4" v-for="card in statCards" :key="card.label">
                <el-card class="stat-card" shadow="hover">
                  <div class="stat-label">{{ card.label }}</div>
                  <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>

          <!-- ==================== 用户管理 ==================== -->
          <el-tab-pane label="用户管理" name="users">
            <el-table :data="users" border stripe v-loading="loadingUsers" style="width: 100%">
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="username" label="用户名" width="150" />
              <el-table-column prop="email" label="邮箱" width="200" />
              <el-table-column prop="role" label="角色" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">{{ row.role }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="注册时间" width="180">
                <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="160">
                <template #default="{ row }">
                  <el-button text type="primary" size="small" @click="openEditUser(row)">编辑</el-button>
                  <el-button text type="danger" size="small" @click="handleDeleteUser(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- ==================== 简历管理 ==================== -->
          <el-tab-pane label="简历管理" name="resumes">
            <el-select v-model="selectedUserId" placeholder="选择用户" @change="loadUserResumes" clearable style="width: 260px; margin-bottom: 16px;">
              <el-option v-for="u in users" :key="u.id" :label="`${u.username} (${u.id})`" :value="u.id" />
            </el-select>
            <el-table :data="userResumes" border stripe v-if="selectedUserId" style="width: 100%">
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="title" label="标题" width="200" />
              <el-table-column prop="version" label="版本" width="80" />
              <el-table-column prop="isCurrent" label="当前版本" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.isCurrent ? 'success' : 'info'" size="small">{{ row.isCurrent ? '是' : '否' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button text type="danger" size="small" @click="handleDeleteResume(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="请先选择一个用户" />
          </el-tab-pane>

          <!-- ==================== 模板管理 ==================== -->
          <el-tab-pane label="模板管理" name="templates">
            <div style="margin-bottom: 12px;">
              <el-button type="primary" @click="openCreateTemplate" :icon="Plus">新建模板</el-button>
            </div>
            <el-table :data="templates" border stripe v-loading="loadingTemplates" style="width: 100%">
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="name" label="名称" width="180" />
              <el-table-column prop="category" label="分类" width="120">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.category }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="160">
                <template #default="{ row }">
                  <el-button text type="primary" size="small" @click="openEditTemplate(row)">编辑</el-button>
                  <el-button text type="danger" size="small" @click="handleDeleteTemplate(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- ==================== 优化日志 ==================== -->
          <el-tab-pane label="优化日志" name="logs">
            <el-table :data="logs" border stripe v-loading="loadingLogs" style="width: 100%">
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="resume.id" label="简历ID" width="70" />
              <el-table-column prop="sectionType" label="区块类型" width="100" />
              <el-table-column prop="llmModel" label="模型" width="120" />
              <el-table-column prop="responseTimeMs" label="响应时间(ms)" width="120" />
              <el-table-column prop="inputText" label="输入" min-width="150" show-overflow-tooltip />
              <el-table-column prop="outputText" label="输出" min-width="150" show-overflow-tooltip />
              <el-table-column prop="createdAt" label="时间" width="160">
                <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-if="logsTotal > 0"
              style="margin-top: 16px; justify-content: flex-end;"
              background layout="total, prev, pager, next"
              :total="logsTotal" :page-size="logsPageSize" v-model:current-page="logsPage"
              @current-change="loadLogs"
            />
          </el-tab-pane>

          <!-- ==================== 匹配记录 ==================== -->
          <el-tab-pane label="匹配记录" name="analyses">
            <el-table :data="analyses" border stripe v-loading="loadingAnalyses" style="width: 100%">
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="user.id" label="用户ID" width="70" />
              <el-table-column prop="resume.id" label="简历ID" width="70" />
              <el-table-column prop="matchScore" label="匹配分" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.matchScore >= 70 ? 'success' : row.matchScore >= 40 ? 'warning' : 'danger'" size="small">
                    {{ row.matchScore }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="jobDescription" label="职位描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="createdAt" label="时间" width="160">
                <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-if="analysesTotal > 0"
              style="margin-top: 16px; justify-content: flex-end;"
              background layout="total, prev, pager, next"
              :total="analysesTotal" :page-size="analysesPageSize" v-model:current-page="analysesPage"
              @current-change="loadAnalyses"
            />
          </el-tab-pane>

          <!-- ==================== 面试记录 ==================== -->
          <el-tab-pane label="面试记录" name="interviews">
            <el-table :data="interviews" border stripe v-loading="loadingInterviews" style="width: 100%">
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="user.id" label="用户ID" width="70" />
              <el-table-column prop="resume.title" label="简历" width="150" show-overflow-tooltip />
              <el-table-column prop="position" label="意向岗位" width="130" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'primary'" size="small">
                    {{ row.status === 'COMPLETED' ? '已完成' : '进行中' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="score" label="评分" width="70" />
              <el-table-column prop="createdAt" label="创建时间" width="160">
                <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column prop="completedAt" label="完成时间" width="160">
                <template #default="{ row }">{{ fmt(row.completedAt) }}</template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-if="interviewsTotal > 0"
              style="margin-top: 16px; justify-content: flex-end;"
              background layout="total, prev, pager, next"
              :total="interviewsTotal" :page-size="interviewsPageSize" v-model:current-page="interviewsPage"
              @current-change="loadInterviews"
            />
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- ===== User Edit Dialog ===== -->
    <el-dialog v-model="showUserDialog" title="编辑用户" width="500px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="userForm.password" placeholder="留空则不修改" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUserDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== Template Edit Dialog ===== -->
    <el-dialog v-model="showTemplateDialog" :title="editingTemplate?.id ? '编辑模板' : '新建模板'" width="600px">
      <el-form :model="templateForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="templateForm.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="templateForm.category" placeholder="例如：技术开发、产品设计、市场运营" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="templateForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="来源URL">
          <el-input v-model="templateForm.sourceUrl" placeholder="可选" />
        </el-form-item>
        <el-form-item label="JSON内容">
          <el-input v-model="templateForm.contentJson" type="textarea" :rows="8" placeholder="模板JSON内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTemplateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate" :loading="saving">
          {{ editingTemplate?.id ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getUsers, updateUser, deleteUser, getUserResumes, deleteUserResume, getAdminTemplates, createTemplate, updateTemplate, deleteTemplate, getStats, getLogs, getAnalyses, getInterviews } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('stats')

// ===== Statistics =====
const loadingStats = ref(false)
const stats = ref({ totalUsers: 0, totalResumes: 0, totalTemplates: 0, totalOptimizationLogs: 0, totalJobAnalyses: 0 })
const statCards = computed(() => [
  { label: '用户总数', value: stats.value.totalUsers, color: '#409EFF' },
  { label: '简历总数', value: stats.value.totalResumes, color: '#67C23A' },
  { label: '模板总数', value: stats.value.totalTemplates, color: '#E6A23C' },
  { label: 'AI优化次数', value: stats.value.totalOptimizationLogs, color: '#909399' },
  { label: '匹配分析次数', value: stats.value.totalJobAnalyses, color: '#F56C6C' },
  { label: '面试次数', value: stats.value.totalInterviewSessions || 0, color: '#9B59B6' }
])

async function loadStats() {
  loadingStats.value = true
  try {
    const res = await getStats()
    stats.value = res.data
  } catch (e) { /* handled */ }
  finally { loadingStats.value = false }
}

// ===== User Management =====
const users = ref([])
const loadingUsers = ref(false)
const showUserDialog = ref(false)
const editingUser = ref(null)
const saving = ref(false)
const userForm = ref({ username: '', email: '', password: '', role: '' })

async function loadUsers() {
  loadingUsers.value = true
  try {
    const res = await getUsers()
    users.value = res.data
  } catch (e) { /* handled */ }
  finally { loadingUsers.value = false }
}

function openEditUser(user) {
  editingUser.value = user
  userForm.value = { username: user.username, email: user.email || '', password: '', role: user.role }
  showUserDialog.value = true
}

async function saveUser() {
  saving.value = true
  try {
    const payload = {}
    if (userForm.value.username) payload.username = userForm.value.username
    if (userForm.value.email !== undefined) payload.email = userForm.value.email
    if (userForm.value.password) payload.password = userForm.value.password
    payload.role = userForm.value.role
    await updateUser(editingUser.value.id, payload)
    ElMessage.success('保存成功')
    showUserDialog.value = false
    await loadUsers()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function handleDeleteUser(user) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？该用户的所有简历也会被删除。`, '确认删除', { type: 'warning' })
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    selectedUserId.value = null
    userResumes.value = []
    await loadUsers()
    await loadStats()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

// ===== Resume Management =====
const selectedUserId = ref(null)
const userResumes = ref([])

async function loadUserResumes(uid) {
  if (!uid) { userResumes.value = []; return }
  try {
    const res = await getUserResumes(uid)
    userResumes.value = res.data
  } catch (e) { /* handled */ }
}

async function handleDeleteResume(resume) {
  try {
    await ElMessageBox.confirm(`确定要删除简历 "${resume.title}" 吗？`, '确认删除', { type: 'warning' })
    await deleteUserResume(selectedUserId.value, resume.id)
    ElMessage.success('删除成功')
    await loadUserResumes(selectedUserId.value)
    await loadStats()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

// ===== Template Management =====
const templates = ref([])
const loadingTemplates = ref(false)
const showTemplateDialog = ref(false)
const editingTemplate = ref(null)
const templateForm = ref({ name: '', category: '', description: '', contentJson: '', sourceUrl: '' })

async function loadTemplates() {
  loadingTemplates.value = true
  try {
    const res = await getAdminTemplates()
    templates.value = res.data
  } catch (e) { /* handled */ }
  finally { loadingTemplates.value = false }
}

function openCreateTemplate() {
  editingTemplate.value = null
  templateForm.value = { name: '', category: '', description: '', contentJson: '', sourceUrl: '' }
  showTemplateDialog.value = true
}

function openEditTemplate(tpl) {
  editingTemplate.value = tpl
  templateForm.value = {
    name: tpl.name, category: tpl.category || '', description: tpl.description || '',
    contentJson: tpl.contentJson, sourceUrl: tpl.sourceUrl || ''
  }
  showTemplateDialog.value = true
}

async function saveTemplate() {
  saving.value = true
  try {
    const payload = { ...templateForm.value }
    if (editingTemplate.value?.id) {
      await updateTemplate(editingTemplate.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(payload)
      ElMessage.success('创建成功')
    }
    showTemplateDialog.value = false
    await loadTemplates()
    await loadStats()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function handleDeleteTemplate(tpl) {
  try {
    await ElMessageBox.confirm(`确定要删除模板 "${tpl.name}" 吗？`, '确认删除', { type: 'warning' })
    await deleteTemplate(tpl.id)
    ElMessage.success('删除成功')
    await loadTemplates()
    await loadStats()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

// ===== Optimization Logs =====
const logs = ref([])
const loadingLogs = ref(false)
const logsPage = ref(1)
const logsPageSize = ref(20)
const logsTotal = ref(0)

async function loadLogs(page = 1) {
  loadingLogs.value = true
  try {
    const res = await getLogs(page - 1, logsPageSize.value)
    logs.value = res.data.content
    logsTotal.value = res.data.totalElements
  } catch (e) { /* handled */ }
  finally { loadingLogs.value = false }
}

// ===== Job Analyses =====
const analyses = ref([])
const loadingAnalyses = ref(false)
const analysesPage = ref(1)
const analysesPageSize = ref(20)
const analysesTotal = ref(0)

async function loadAnalyses(page = 1) {
  loadingAnalyses.value = true
  try {
    const res = await getAnalyses(page - 1, analysesPageSize.value)
    analyses.value = res.data.content
    analysesTotal.value = res.data.totalElements
  } catch (e) { /* handled */ }
  finally { loadingAnalyses.value = false }
}

// ===== Tab switching =====
function handleLogout() {
  userStore.logout()
  router.push('/login')
}

// ===== Interview Sessions =====
const interviews = ref([])
const loadingInterviews = ref(false)
const interviewsPage = ref(1)
const interviewsPageSize = ref(20)
const interviewsTotal = ref(0)

async function loadInterviews(page = 1) {
  loadingInterviews.value = true
  try {
    const res = await getInterviews(page - 1, interviewsPageSize.value)
    interviews.value = res.data.content
    interviewsTotal.value = res.data.totalElements
  } catch (e) { /* handled */ }
  finally { loadingInterviews.value = false }
}

function onTabChange(tab) {
  if (tab === 'stats') loadStats()
  else if (tab === 'logs') loadLogs()
  else if (tab === 'analyses') loadAnalyses()
  else if (tab === 'interviews') loadInterviews()
}

function fmt(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadStats()
  loadUsers()
  loadTemplates()
})
</script>

<style scoped>
.admin-page {
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
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
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
.username { color: #606266; }
.stat-card {
  text-align: center;
  padding: 8px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
}
</style>
