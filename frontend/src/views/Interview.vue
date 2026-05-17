<template>
  <div class="interview-page">
    <el-container style="height: 100vh;">
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="goBack" :icon="ArrowLeft">退出面试</el-button>
          <h3>AI模拟面试</h3>
          <el-tag v-if="sessionId" type="warning" size="small">第 {{ questionNumber }} 题</el-tag>
        </div>
        <div class="header-right">
          <el-button type="danger" plain size="small" @click="handleEnd" :disabled="loading || !sessionId">结束面试</el-button>
        </div>
      </el-header>

      <el-main class="chat-area" ref="chatContainer">
        <div v-if="!sessionId" class="start-area">
          <el-card class="start-card">
            <h3>AI 模拟面试</h3>
            <p>AI 面试官将根据你的简历进行模拟面试，帮助你提升面试技巧</p>
            <el-form label-width="80px" style="margin-top: 20px;">
              <el-form-item label="选择简历">
                <el-select v-model="selectedResumeId" placeholder="选择要面试的简历" style="width: 100%;">
                  <el-option v-for="r in resumes" :key="r.id" :label="r.title" :value="r.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="意向岗位">
                <el-input v-model="position" placeholder="例如：Java后端开发工程师" />
              </el-form-item>
            </el-form>
            <el-button type="primary" @click="handleStart" :loading="loading" :disabled="!selectedResumeId" style="width: 100%; margin-top: 12px;">
              开始面试
            </el-button>
          </el-card>
        </div>

        <div v-else class="messages">
          <div v-for="(msg, idx) in messages" :key="idx" class="msg-row" :class="msg.role">
            <div class="msg-bubble">
              <div class="msg-role">{{ msg.role === 'ai' ? '🤖 面试官' : '👤 我' }}</div>
              <div class="msg-text">{{ msg.content }}</div>
            </div>
          </div>
          <div v-if="loading" class="msg-row ai">
            <div class="msg-bubble">
              <div class="msg-role">🤖 面试官</div>
              <div class="msg-text typing">思考中...</div>
            </div>
          </div>
        </div>
      </el-main>

      <div class="input-bar" v-if="sessionId">
        <div class="input-row">
          <el-tooltip :content="voiceSupported ? '点击开始语音输入' : '语音输入仅支持Chrome/Edge浏览器'" placement="top">
            <el-button
              @click="toggleVoice"
              :type="listening ? 'danger' : 'default'"
              :icon="Microphone"
              size="large"
              circle
              class="mic-btn"
              :class="{ active: listening }"
            />
          </el-tooltip>
          <el-input
            v-model="userInput"
            placeholder="输入你的回答..."
            :disabled="loading || isComplete"
            @keyup.enter="sendAnswer"
            size="large"
            class="text-input"
          >
            <template #append>
              <el-button type="primary" @click="sendAnswer" :disabled="!userInput.trim() || loading || isComplete" size="large">发送</el-button>
            </template>
          </el-input>
        </div>
        <div v-if="listening" class="voice-hint">
          <span class="voice-dot"></span> 正在录音，请说话...
        </div>
      </div>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getResumes } from '../api/resume'
import { startInterview, submitAnswer, endInterview } from '../api/interview'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Microphone } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const resumes = ref([])
const selectedResumeId = ref(null)
const position = ref('')
const sessionId = ref(null)
const messages = ref([])
const userInput = ref('')
const loading = ref(false)
const questionNumber = ref(0)
const isComplete = ref(false)
const chatContainer = ref(null)
const listening = ref(false)
const voiceSupported = ref(false)

let recognition = null

function initVoice() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (SpeechRecognition) {
    voiceSupported.value = true
  }
}

function toggleVoice() {
  if (listening.value) {
    if (recognition) recognition.abort()
    listening.value = false
    return
  }
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    ElMessage.warning('当前浏览器不支持语音识别，请使用Chrome或Edge浏览器')
    return
  }
  recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.interimResults = true
  recognition.continuous = false
  recognition.maxAlternatives = 1

  let finalTranscript = ''

  recognition.onresult = (event) => {
    let interim = ''
    for (let i = event.resultIndex; i < event.results.length; i++) {
      const result = event.results[i]
      if (result.isFinal) {
        finalTranscript += result[0].transcript
      } else {
        interim += result[0].transcript
      }
    }
    userInput.value = finalTranscript + interim
  }

  recognition.onerror = (event) => {
    console.error('Speech error:', event.error)
    if (event.error === 'not-allowed') {
      ElMessage.error('麦克风权限被拒绝，请在浏览器设置中允许麦克风访问')
    } else if (event.error === 'no-speech') {
      // silent - no speech detected
    } else if (event.error !== 'aborted') {
      ElMessage.error('语音识别出错: ' + event.error)
    }
    if (finalTranscript) {
      userInput.value = finalTranscript
    }
    listening.value = false
  }

  recognition.onend = () => {
    if (finalTranscript) {
      userInput.value = finalTranscript
    }
    listening.value = false
  }

  try {
    recognition.start()
    listening.value = true
    finalTranscript = ''
    userInput.value = ''
  } catch (e) {
    console.error('Speech start error:', e)
    ElMessage.error('语音识别启动失败，请刷新页面重试')
    listening.value = false
  }
}

async function loadResumes() {
  try {
    const res = await getResumes()
    resumes.value = res.data
    if (res.data.length > 0) selectedResumeId.value = res.data[0].id
  } catch (e) { /* handled */ }
}

async function handleStart() {
  if (!selectedResumeId.value) return
  loading.value = true
  try {
    const res = await startInterview({ resumeId: selectedResumeId.value, position: position.value })
    sessionId.value = res.data.sessionId
    questionNumber.value = res.data.questionNumber
    messages.value = [{ role: 'ai', content: res.data.question }]
    scrollDown()
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function sendAnswer() {
  if (!userInput.value.trim() || loading.value || isComplete.value) return
  const answer = userInput.value.trim()
  userInput.value = ''
  messages.value.push({ role: 'user', content: answer })
  loading.value = true
  scrollDown()
  try {
    const res = await submitAnswer({ sessionId: sessionId.value, answer })
    if (res.data.status === 'COMPLETED') {
      isComplete.value = true
      messages.value.push({ role: 'ai', content: res.data.question })
      ElMessage.success('面试已完成，正在生成报告...')
      setTimeout(() => router.push(`/interview/report?id=${sessionId.value}`), 1500)
    } else {
      messages.value.push({ role: 'ai', content: res.data.question })
      questionNumber.value = res.data.questionNumber
    }
  } catch (e) { /* handled */ }
  finally { loading.value = false; scrollDown() }
}

async function handleEnd() {
  try {
    await ElMessageBox.confirm('确定要结束本次面试吗？将自动生成面试报告。', '结束面试', { type: 'warning' })
    loading.value = true
    const res = await endInterview(sessionId.value)
    ElMessage.success('报告生成完成')
    router.push(`/interview/report?id=${res.data.sessionId}`)
  } catch (e) { if (e !== 'cancel') console.error(e) }
  finally { loading.value = false }
}

function goBack() {
  if (sessionId.value && !isComplete.value) {
    ElMessageBox.confirm('退出将丢失当前面试进度，确定退出吗？', '确认退出', { type: 'warning' })
      .then(() => router.push('/dashboard'))
      .catch(() => {})
  } else {
    router.push('/dashboard')
  }
}

function scrollDown() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

onMounted(() => { loadResumes(); initVoice() })
</script>

<style scoped>
.interview-page { background: #f5f7fa; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left h3 { margin: 0; }
.start-area {
  display: flex; justify-content: center; padding-top: 80px;
}
.start-card { width: 480px; }
.start-card h3 { text-align: center; margin-bottom: 8px; }
.start-card p { text-align: center; color: #909399; font-size: 14px; }
.chat-area { flex: 1; overflow-y: auto; padding: 20px; }
.messages { max-width: 800px; margin: 0 auto; }
.msg-row { margin-bottom: 16px; display: flex; }
.msg-row.user { justify-content: flex-end; }
.msg-row.ai { justify-content: flex-start; }
.msg-bubble {
  max-width: 75%; padding: 12px 16px; border-radius: 10px; line-height: 1.6;
}
.msg-row.ai .msg-bubble { background: #fff; border: 1px solid #e4e7ed; }
.msg-row.user .msg-bubble { background: #409EFF; color: #fff; }
.msg-role { font-size: 12px; margin-bottom: 4px; opacity: 0.7; }
.msg-text { white-space: pre-wrap; word-break: break-word; font-size: 14px; }
.typing { opacity: 0.6; font-style: italic; }
.input-bar { padding: 12px 20px; background: #fff; border-top: 1px solid #ebeef5; max-width: 860px; margin: 0 auto; width: 100%; }
.input-row { display: flex; align-items: center; gap: 10px; }
.mic-btn { flex-shrink: 0; }
.mic-btn.active {
  animation: pulse 1.2s infinite;
  box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.6);
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.6); }
  70% { box-shadow: 0 0 0 12px rgba(245, 108, 108, 0); }
  100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}
.text-input { flex: 1; }
.voice-hint {
  text-align: center; color: #E6A23C; font-size: 14px; margin-top: 8px;
  display: flex; align-items: center; justify-content: center; gap: 8px;
}
.voice-dot {
  display: inline-block; width: 10px; height: 10px;
  background: #F56C6C; border-radius: 50%;
  animation: blink 0.8s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.2; }
}
</style>
