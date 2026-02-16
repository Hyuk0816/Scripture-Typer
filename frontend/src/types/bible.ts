export interface BookSummary {
  bookName: string
  bookOrder: number
  totalChapters: number
}

export interface BooksResponse {
  oldTestament: BookSummary[]
  newTestament: BookSummary[]
}

export interface VerseData {
  id: number
  verse: number
  content: string
}

export interface ChapterResponse {
  bookName: string
  chapter: number
  verses: VerseData[]
}