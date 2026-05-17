<template>
  <div class="resume-form" id="resume-preview">
    <h3>简历编辑</h3>

    <!-- Basic Info -->
    <el-divider content-position="left">基本信息</el-divider>
    <el-form label-width="80px" size="small">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="姓名">
            <el-input v-model="formData.basicInfo.name" placeholder="请输入姓名" @input="emitUpdate" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="求职意向">
            <el-input v-model="formData.basicInfo.position" placeholder="目标职位" @input="emitUpdate" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="电话">
            <el-input v-model="formData.basicInfo.phone" placeholder="手机号" @input="emitUpdate" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱">
            <el-input v-model="formData.basicInfo.email" placeholder="邮箱地址" @input="emitUpdate" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="证件照">
            <div class="photo-upload">
              <div class="photo-preview" v-if="formData.basicInfo.photo">
                <img :src="formData.basicInfo.photo" alt="证件照" />
                <el-button type="danger" size="small" circle class="photo-remove" @click="removePhoto" :icon="Delete" />
              </div>
              <el-upload
                v-else
                :auto-upload="false"
                :show-file-list="false"
                accept="image/*"
                :on-change="handlePhotoChange"
                drag
              >
                <el-icon :size="28"><Plus /></el-icon>
                <div class="upload-text">上传证件照</div>
              </el-upload>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- Summary -->
    <el-divider content-position="left">个人简介</el-divider>
    <div class="section-actions">
      <el-input v-model="formData.summary" type="textarea" :rows="3" placeholder="简要描述个人优势及职业目标" @input="emitUpdate" />
      <el-button text type="primary" size="small" @click="openAI('summary')" :icon="MagicStick">AI优化</el-button>
    </div>

    <!-- Work Experience -->
    <el-divider content-position="left">
      工作经历
      <el-button text type="primary" size="small" @click="addWork" :icon="Plus">添加</el-button>
    </el-divider>
    <div v-for="(item, index) in formData.workExperience" :key="index" class="section-item">
      <el-form label-width="70px" size="small">
        <el-row :gutter="8">
          <el-col :span="8">
            <el-form-item label="公司">
              <el-input v-model="item.company" placeholder="公司名称" @input="emitUpdate" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职位">
              <el-input v-model="item.position" placeholder="职位名称" @input="emitUpdate" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="时间">
              <el-input v-model="item.period" placeholder="起止时间" @input="emitUpdate" />
            </el-form-item>
          </el-col>
          <el-col :span="1">
            <el-button text type="danger" :icon="Delete" @click="removeWork(index)" />
          </el-col>
        </el-row>
        <el-form-item label="职责">
          <div class="inline-actions">
            <el-input v-model="item.description" type="textarea" :rows="3" placeholder="描述工作职责和成果" @input="emitUpdate" />
            <el-button text type="primary" size="small" @click="openAI('workExperience', index)" :icon="MagicStick">AI优化</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- Education -->
    <el-divider content-position="left">
      教育背景
      <el-button text type="primary" size="small" @click="addEducation" :icon="Plus">添加</el-button>
    </el-divider>
    <div v-for="(item, index) in formData.education" :key="index" class="section-item">
      <el-form label-width="70px" size="small">
        <el-row :gutter="8">
          <el-col :span="8">
            <el-form-item label="学校">
              <el-input v-model="item.school" placeholder="学校名称" @input="emitUpdate" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="专业">
              <el-input v-model="item.major" placeholder="专业" @input="emitUpdate" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="学历">
              <el-select v-model="item.degree" placeholder="学历" style="width: 100%;" @change="emitUpdate">
                <el-option label="博士" value="博士" />
                <el-option label="硕士" value="硕士" />
                <el-option label="本科" value="本科" />
                <el-option label="大专" value="大专" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="1">
            <el-button text type="danger" :icon="Delete" @click="removeEducation(index)" />
          </el-col>
        </el-row>
        <el-form-item label="时间">
          <el-input v-model="item.period" placeholder="起止时间" @input="emitUpdate" />
        </el-form-item>
      </el-form>
    </div>

    <!-- Skills -->
    <el-divider content-position="left">专业技能</el-divider>
    <div class="section-actions">
      <el-input v-model="formData.skills" type="textarea" :rows="3" placeholder="如：Java, Spring Boot, MySQL, Redis..." @input="emitUpdate" />
      <el-button text type="primary" size="small" @click="openAI('skills')" :icon="MagicStick">AI推荐</el-button>
    </div>

    <!-- Projects -->
    <el-divider content-position="left">
      项目经历
      <el-button text type="primary" size="small" @click="addProject" :icon="Plus">添加</el-button>
    </el-divider>
    <div v-for="(item, index) in formData.projects" :key="index" class="section-item">
      <el-form label-width="70px" size="small">
        <el-row :gutter="8">
          <el-col :span="11">
            <el-form-item label="项目">
              <el-input v-model="item.name" placeholder="项目名称" @input="emitUpdate" />
            </el-form-item>
          </el-col>
          <el-col :span="11">
            <el-form-item label="角色">
              <el-input v-model="item.role" placeholder="你的角色" @input="emitUpdate" />
            </el-form-item>
          </el-col>
          <el-col :span="1">
            <el-button text type="danger" :icon="Delete" @click="removeProject(index)" />
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <div class="inline-actions">
            <el-input v-model="item.description" type="textarea" :rows="3" placeholder="项目描述及你的贡献" @input="emitUpdate" />
            <el-button text type="primary" size="small" @click="openAI('projects', index)" :icon="MagicStick">AI优化</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { MagicStick, Plus, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  resumeData: { type: Object, default: null }
})

const emit = defineEmits(['update', 'ai-optimize'])

const formData = reactive({
  basicInfo: { name: '', phone: '', email: '', position: '', photo: '' },
  summary: '',
  workExperience: [],
  education: [],
  skills: '',
  projects: []
})

watch(() => props.resumeData, (val) => {
  if (val) {
    Object.assign(formData, JSON.parse(JSON.stringify(val)))
  }
}, { immediate: true, deep: true })

function emitUpdate() {
  emit('update', { ...formData })
}

function addWork() {
  formData.workExperience.push({ company: '', position: '', period: '', description: '' })
}
function removeWork(index) {
  formData.workExperience.splice(index, 1)
  emitUpdate()
}

function addEducation() {
  formData.education.push({ school: '', major: '', degree: '', period: '' })
}
function removeEducation(index) {
  formData.education.splice(index, 1)
  emitUpdate()
}

function addProject() {
  formData.projects.push({ name: '', role: '', description: '' })
}
function removeProject(index) {
  formData.projects.splice(index, 1)
  emitUpdate()
}

function handlePhotoChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    formData.basicInfo.photo = e.target.result
    emitUpdate()
  }
  reader.readAsDataURL(file.raw)
}

function removePhoto() {
  formData.basicInfo.photo = ''
  emitUpdate()
}

function openAI(section, index) {
  let text = ''
  if (section === 'workExperience' && index !== undefined) {
    text = formData.workExperience[index]?.description || ''
  } else if (section === 'projects' && index !== undefined) {
    text = formData.projects[index]?.description || ''
  } else if (section === 'summary') {
    text = formData.summary || ''
  } else if (section === 'skills') {
    text = formData.skills || ''
  }

  emit('ai-optimize', { section, index, text })
}
</script>

<style scoped>
.resume-form {
  padding: 8px;
}
.resume-form h3 {
  margin: 0 0 8px;
}
.section-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  background: #fafafa;
}
.section-actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.inline-actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}
.inline-actions .el-textarea {
  flex: 1;
}
.photo-upload {
  width: 120px;
}
.photo-preview {
  position: relative;
  width: 100px;
  height: 130px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}
.photo-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.photo-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  z-index: 1;
}
.upload-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
