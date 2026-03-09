export interface GroupPlanRequest {
  bookName: string
  startChapter: number
  endChapter: number
  mode: 'READING' | 'TYPING'
}

export interface GroupPlanResponse {
  id: number
  bookName: string
  startChapter: number
  endChapter: number
  mode: 'READING' | 'TYPING'
  status: 'ACTIVE' | 'COMPLETED'
  affiliationName: string
  createdByName: string
  createdAt: string
}

export interface GroupMemberProgressResponse {
  userId: number
  userName: string
  completedChapters: number
  totalReadCount: number
}

export interface GroupPlanDetailResponse {
  id: number
  bookName: string
  startChapter: number
  endChapter: number
  mode: 'READING' | 'TYPING'
  status: 'ACTIVE' | 'COMPLETED'
  affiliationName: string
  createdByName: string
  createdAt: string
  totalChapters: number
  members: GroupMemberProgressResponse[]
}
