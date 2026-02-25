export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

export interface ChatSession {
  id: number
  title: string
  bookName: string | null
  chapter: number | null
  messageCount: number
  updatedAt: string
}

export interface ChatUsage {
  used: number
  limit: number
  unlimited: boolean
}
