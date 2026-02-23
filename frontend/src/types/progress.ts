export interface SaveReadingProgressRequest {
  bookName: string
  chapter: number
  lastReadVerse: number
}

export interface CompleteReadingRequest {
  bookName: string
  chapter: number
}

export interface ReadingProgressResponse {
  bookName: string
  chapter: number
  lastReadVerse: number
  readCount: number
}

export interface SaveTypingProgressRequest {
  bookName: string
  chapter: number
  lastTypedVerse: number
}

export interface CompleteTypingRequest {
  bookName: string
  chapter: number
}

export interface TypingProgressResponse {
  bookName: string
  chapter: number
  lastTypedVerse: number
  readCount: number
  totalVerses: number
}

export interface RankingEntryResponse {
  rank: number
  userId: number
  name: string
  completedChapters: number
}