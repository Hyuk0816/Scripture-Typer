export type PostType = 'NOTICE' | 'BIBLE_QUESTION' | 'FREE' | 'SUGGESTION'

export interface BoardListItem {
  id: number
  postType: PostType
  title: string
  authorName: string
  authorRole: string
  replyCount: number
  createdAt: string
}

export interface BoardDetail {
  id: number
  postType: PostType
  title: string
  content: string
  authorId: number
  authorName: string
  authorRole: string
  replies: ReplyItem[]
  createdAt: string
  updatedAt: string
}

export interface ReplyItem {
  id: number
  content: string
  authorId: number
  authorName: string
  authorRole: string
  createdAt: string
  updatedAt: string
}

export interface BoardRequest {
  postType: PostType
  title: string
  content: string
}

export interface ReplyRequest {
  content: string
}

export interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
}
