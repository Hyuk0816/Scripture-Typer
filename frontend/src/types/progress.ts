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