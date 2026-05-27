<template>
  <el-card class="ai-panel">
    <template #header>
      <div class="panel-header">
        <span><el-icon :size="18"><MagicStick /></el-icon> AI 优化助手</span>
      </div>
    </template>

    <div class="chat-container" ref="chatContainer">
      <div v-if="messages.length === 0" class="empty-chat">
        <el-empty description="在简历中选择一个区块，点击 AI 优化按钮开始" />
      </div>
      <div v-for="(msg, idx) in messages" :key="idx" class="message" :class="msg.role">
        <div class="msg-content">
          <div class="msg-label">{{ msg.role === 'ai' ? 'AI' : '我' }}</div>
          <div class="msg-text">{{ msg.content }}</div>
          <div v-if="msg.optimized" class="msg-actions">
            <el-button size="small" type="primary" @click="applySuggestion(msg)">应用修改</el-button>
            <el-button size="small" @click="regenerate(msg.section)">重新生成</el-button>
          </div>
        </div>
      </div>
      <div v-if="loading" class="message ai">
        <div class="msg-content">
          <div class="msg-label">AI</div>
          <div class="msg-text"><el-icon class="is-loading"><Loading /></el-icon> 正在优化...</div>
        </div>
      </div>
    </div>

    <div class="input-area">
      <el-input
        v-model="userInput"
        :disabled="!currentSection"
        placeholder="输入额外的优化要求..."
        size="small"
        @keyup.enter="sendMessage"
      >
        <template #append>
          <el-button @click="sendMessage" :disabled="!userInput.trim() || loading" :icon="Promotion">发送</el-button>
        </template>
      </el-input>
    </div>
  </el-card>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { optimizeSection } from '../api/ai'
import { ElMessage } from 'element-plus'
import { MagicStick, Promotion, Loading } from '@element-plus/icons-vue'

const props = defineProps({
  resumeId: { type: Number, required: true },
  resumeData: { type: Object, default: null }
})

const emit = defineEmits(['apply-optimization'])

const messages = ref([])
const userInput = ref('')
const loading = ref(false)
const currentSection = ref('')
const currentSectionIndex = ref(undefined)
const originalText = ref('')
const chatContainer = ref(null)

function setSection(section, index, text) {
  currentSection.value = section
  currentSectionIndex.value = index
  originalText.value = text

  messages.value.push({
    role: 'user',
    content: `请优化我的${getSectionLabel(section)}${index !== undefined ? ` #${index + 1}` : ''}`,
    section,
    index
  })

  doOptimize(text, '')
}

async function doOptimize(text, instruction) {
  loading.value = true
  try {
    const res = await optimizeSection({
      resumeId: props.resumeId,
      sectionType: currentSection.value,
      originalText: text,
      instruction: instruction || undefined
    })
    messages.value.push({
      role: 'ai',
      content: res.data.optimizedText,
      optimized: true,
      section: currentSection.value,
      index: currentSectionIndex.value
    })
    scrollToBottom()
  } catch (e) {
    messages.value.push({
      role: 'ai',
      content: '优化服务暂时不可用，请稍后再试。',
      optimized: false
    })
  } finally {
    loading.value = false
  }
}

async function sendMessage() {
  if (!userInput.value.trim() || !currentSection.value || loading.value) return

  const instruction = userInput.value
  messages.value.push({
    role: 'user',
    content: instruction
  })
  userInput.value = ''

  await doOptimize(originalText.value, instruction)
}

function regenerate(section) {
  // Remove last AI message if any
  if (messages.value.length > 0 && messages.value[messages.value.length - 1].role === 'ai') {
    messages.value.pop()
  }
  doOptimize(originalText.value, '')
}

function applySuggestion(msg) {
  const section = msg.section
  const index = msg.index
  const text = msg.content

  emit('apply-optimization', { section, index, text })
  ElMessage.success('已应用到简历')
}

function getSectionLabel(section) {
  const labels = {
    summary: '个人简介',
    workExperience: '工作经历',
    education: '教育背景',
    skills: '专业技能',
    projects: '项目经历'
  }
  return labels[section] || section
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// Expose setSection for parent to call
defineExpose({ setSection })

watch(() => props.resumeId, () => {
  // clear messages when resume changes
  messages.value = []
  currentSection.value = ''
})
</script>

<style scoped>
.ai-panel {
  height: calc(100vh - 90px);
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  align-items: center;
  gap: 6px;
}
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  margin-bottom: 8px;
}
.empty-chat {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.message {
  margin-bottom: 12px;
  display: flex;
}
.message.user {
  justify-content: flex-end;
}
.message.ai {
  justify-content: flex-start;
}
.msg-content {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.5;
}
.message.user .msg-content {
  background: #ecf5ff;
  color: #303133;
}
.message.ai .msg-content {
  background: #f0f9eb;
  color: #303133;
}
.msg-label {
  font-size: 11px;
  font-weight: bold;
  margin-bottom: 4px;
  color: #909399;
}
.msg-text {
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-actions {
  margin-top: 8px;
  display: flex;
  gap: 6px;
}
.input-area {
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
}
</style>
