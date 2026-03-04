import { ref } from 'vue'
import { defineStore } from 'pinia'
import { useChatStore } from '@/stores/chat'

export type Testament = 'OLD' | 'NEW'

export const useUiStore = defineStore('ui', () => {
  const sidebarOpen = ref(true)
  const activeTestament = ref<Testament>('OLD')
  const expandedBook = ref<string | null>(null)

  function toggleSidebar() {
    const opening = !sidebarOpen.value
    sidebarOpen.value = opening
    if (opening) {
      const chatStore = useChatStore()
      if (chatStore.isOpen) chatStore.toggleChat()
    }
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