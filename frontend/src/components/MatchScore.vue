<template>
  <el-card>
    <template #header>
      <span>匹配度分析结果</span>
    </template>

    <div class="score-section" v-if="result">
      <div class="score-circle">
        <el-progress type="circle" :percentage="result.score" :stroke-width="10" :width="160"
          :color="scoreColor" />
        <p class="score-label">综合匹配度</p>
      </div>

      <el-divider />

      <div class="detail-section">
        <h4>优势</h4>
        <ul>
          <li v-for="(item, i) in result.strengths" :key="i">{{ item }}</li>
        </ul>
        <el-empty v-if="!result.strengths?.length" description="暂无数据" />
      </div>

      <div class="detail-section">
        <h4>不足</h4>
        <ul>
          <li v-for="(item, i) in result.weaknesses" :key="i" class="weakness">{{ item }}</li>
        </ul>
        <el-empty v-if="!result.weaknesses?.length" description="暂无数据" />
      </div>

      <div class="detail-section">
        <h4>改进建议</h4>
        <el-timeline>
          <el-timeline-item v-for="(item, i) in result.suggestions" :key="i" :timestamp="`建议 ${i + 1}`">
            {{ item }}
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!result.suggestions?.length" description="暂无数据" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: { type: Object, default: null }
})

const scoreColor = computed(() => {
  if (!props.result) return '#409eff'
  const score = props.result.score
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
})
</script>

<style scoped>
.score-section {
  text-align: center;
}
.score-circle {
  padding: 20px 0;
}
.score-label {
  margin-top: 12px;
  color: #909399;
  font-size: 14px;
}
.detail-section {
  text-align: left;
  margin: 16px 0;
}
.detail-section h4 {
  margin: 0 0 8px;
  color: #303133;
}
.detail-section ul {
  padding-left: 20px;
}
.detail-section li {
  margin-bottom: 6px;
  line-height: 1.5;
  color: #606266;
}
.weakness {
  color: #f56c6c;
}
</style>
