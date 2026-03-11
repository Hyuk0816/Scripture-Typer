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

export interface AffiliationRankingResponse {
  affiliationName: string
  mainAffiliation: string | null
  myRank: number
  myCompletedChapters: number
  rankings: RankingEntryResponse[]
}

export interface GroupRankingResponse {
  rank: number
  affiliationId: number
  affiliationName: string
  totalCompletedChapters: number
  memberCount: number
}

export type RankingMode = 'READING' | 'TYPING'