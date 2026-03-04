export interface UserLoginDetail {
  userId: number
  userName: string
  loginCount: number
}

export interface DailyLoginStatResponse {
  date: string
  totalLogins: number
  users: UserLoginDetail[]
}

export interface UserProgressDetail {
  userId: number
  userName: string
  readingCount: number
  typingCount: number
}

export interface DailyProgressStatResponse {
  date: string
  users: UserProgressDetail[]
}

export interface UserChatDetail {
  userId: number
  userName: string
  questionCount: number
}

export interface DailyChatStatResponse {
  date: string
  users: UserChatDetail[]
}

export interface MonthlyStatResponse {
  year: number
  month: number
  totalActiveUsers: number
  totalReadingCompleted: number
  totalTypingCompleted: number
  totalChatQuestions: number
}
