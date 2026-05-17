import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useResumeStore = defineStore('resume', () => {
  const currentResume = ref(null)
  const resumeList = ref([])

  function setCurrentResume(resume) {
    currentResume.value = resume
  }

  function setResumeList(list) {
    resumeList.value = list
  }

  return { currentResume, resumeList, setCurrentResume, setResumeList }
})
