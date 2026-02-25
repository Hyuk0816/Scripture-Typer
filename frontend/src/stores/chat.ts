import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { ChatMessage, ChatSession, ChatUsage } from '@/types/chat'
import { chatApi, getAccessToken } from '@/utils/api'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const isOpen = ref(false)
  const isLoading = ref(false)
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<number | null>(null)
  const view = ref<'chat' | 'list'>('chat')
  const usage = ref<ChatUsage>({ used: 0, limit: 5, unlimited: false })

  function toggleChat() {
    isOpen.value = !isOpen.value
  }

  function setView(v: 'chat' | 'list') {
    view.value = v
    if (v === 'list') {
      fetchSessions()
    }
  }

  async function fetchSessions() {
    try {
      const { data } = await chatApi.getSessions()
      sessions.value = data
    } catch {
      // silent
    }
  }

  async function selectSession(id: number) {
    try {
      const { data: dbMessages } = await chatApi.getMessages(id)
      messages.value = dbMessages.map((m) => ({
        id: String(m.id),
        role: m.role as ChatMessage['role'],
        content: m.content,
        timestamp: new Date(m.createdAt).getTime(),
      }))
      currentSessionId.value = id
      view.value = 'chat'
    } catch {
      // silent
    }
  }

  function startNewSession() {
    currentSessionId.value = null
    messages.value = []
    view.value = 'chat'
  }

  async function deleteSession(id: number) {
    try {
      await chatApi.deleteSession(id)
      if (currentSessionId.value === id) {
        currentSessionId.value = null
        messages.value = []
      }
      await fetchSessions()
    } catch {
      // silent
    }
  }

  async function fetchUsage() {
    try {
      const { data } = await chatApi.getUsage()
      usage.value = data
    } catch {
      // silent
    }
  }

  async function sendMessage(
    content: string,
    context?: { bookName: string; chapter: number; verse?: number },
  ) {
    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content,
      timestamp: Date.now(),
    }

    const assistantMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
    }

    messages.value = [...messages.value, userMessage, assistantMessage]
    isLoading.value = true

    // Ensure session exists
    let sessionId = currentSessionId.value
    if (!sessionId) {
      try {
        const { data: session } = await chatApi.createSession({
          title: content.slice(0, 30),
          bookName: context?.bookName ?? null,
          chapter: context?.chapter ?? null,
        })
        sessionId = session.id
        currentSessionId.value = sessionId
      } catch {
        // continue without persistence
      }
    }

    // Save user message to DB
    if (sessionId) {
      chatApi.addMessage(sessionId, { role: 'user', content }).catch(() => {})
    }

    // Build messages for Gemini (exclude the empty assistant placeholder)
    const chatMessages = messages.value
      .filter((m) => m.id !== assistantMessage.id)
      .map((m) => ({ role: m.role, content: m.content }))

    let fullAssistantContent = ''

    try {
      const token = getAccessToken()
      const res = await fetch('/api/chat/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({
          messages: chatMessages,
          context: context ?? null,
        }),
      })

      if (!res.ok) {
        if (res.status === 429) {
          updateAssistantMessage(assistantMessage.id, '오늘의 채팅 사용량을 초과했습니다.')
          isLoading.value = false
          await fetchUsage()
          return
        }
        throw new Error(`HTTP ${res.status}`)
      }

      const reader = res.body?.getReader()
      if (!reader) throw new Error('No response stream')

      const decoder = new TextDecoder()
      let sseBuffer = ''
      let streamDone = false

      while (!streamDone) {
        const { done, value } = await reader.read()
        if (done) break

        sseBuffer += decoder.decode(value, { stream: true })

        // 완전한 라인만 처리, 불완전한 마지막 라인은 버퍼에 유지
        const lastNewline = sseBuffer.lastIndexOf('\n')
        if (lastNewline < 0) continue

        const complete = sseBuffer.slice(0, lastNewline + 1)
        sseBuffer = sseBuffer.slice(lastNewline + 1)

        const lines = complete.split('\n').filter((l) => l.startsWith('data:'))

        for (const line of lines) {
          const data = line.slice(5).trim()
          if (data === '[DONE]') {
            streamDone = true
            break
          }

          try {
            const parsed = JSON.parse(data)
            if (parsed.error) {
              fullAssistantContent = `오류: ${parsed.error}`
              updateAssistantMessage(assistantMessage.id, fullAssistantContent)
              streamDone = true
              break
            }
            if (parsed.content) {
              fullAssistantContent += parsed.content
              updateAssistantMessage(assistantMessage.id, fullAssistantContent)
            }
          } catch {
            // skip malformed
          }
        }
      }

      // Save assistant response to DB
      if (sessionId && fullAssistantContent) {
        chatApi
          .addMessage(sessionId, { role: 'assistant', content: fullAssistantContent })
          .catch(() => {})
      }

      await fetchUsage()
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Unknown error'
      updateAssistantMessage(assistantMessage.id, `연결 오류: ${errorMsg}`)
    } finally {
      isLoading.value = false
    }
  }

  function updateAssistantMessage(id: string, content: string) {
    messages.value = messages.value.map((m) => (m.id === id ? { ...m, content } : m))
  }

  function clearMessages() {
    messages.value = []
    currentSessionId.value = null
  }

  return {
    messages,
    isOpen,
    isLoading,
    sessions,
    currentSessionId,
    view,
    usage,
    toggleChat,
    setView,
    fetchSessions,
    selectSession,
    startNewSession,
    deleteSession,
    fetchUsage,
    sendMessage,
    clearMessages,
  }
})
