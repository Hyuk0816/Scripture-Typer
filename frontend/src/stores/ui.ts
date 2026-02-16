import { ref } from 'vue'
import { defineStore } from 'pinia'

export type Testament = 'OLD' | 'NEW'

export const useUiStore = defineStore('ui', () => {
  const sidebarOpen = ref(true)
  const activeTestament = ref<Testament>('OLD')
  const expandedBook = ref<string | null>(null)

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function setActiveTestament(testament: Testament) {
    activeTestament.value = testament
    expandedBook.value = null
  }

  function setExpandedBook(bookName: string | null) {
    expandedBook.value = bookName
  }

  return {
    sidebarOpen,
    activeTestament,
    expandedBook,
    toggleSidebar,
    setActiveTestament,
    setExpandedBook,
  }
})