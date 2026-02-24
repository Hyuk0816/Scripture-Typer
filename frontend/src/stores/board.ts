import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { BoardListItem, BoardDetail, BoardRequest, ReplyRequest } from '@/types/board'
import { boardApi } from '@/utils/api'

export const useBoardStore = defineStore('board', () => {
  const boards = ref<BoardListItem[]>([])
  const currentBoard = ref<BoardDetail | null>(null)
  const totalPages = ref(0)
  const currentPage = ref(0)
  const selectedPostType = ref<string | undefined>(undefined)
  const loading = ref(false)

  async function fetchBoards(page = 0, size = 10, postType?: string) {
    loading.value = true
    try {
      const { data } = await boardApi.getBoards(page, size, postType)
      boards.value = data.content
      totalPages.value = data.totalPages
      currentPage.value = data.number
      selectedPostType.value = postType
    } finally {
      loading.value = false
    }
  }

  async function fetchBoard(id: number) {
    loading.value = true
    try {
      const { data } = await boardApi.getBoard(id)
      currentBoard.value = data
    } finally {
      loading.value = false
    }
  }

  async function createBoard(request: BoardRequest): Promise<void> {
    await boardApi.createBoard(request)
  }

  async function updateBoard(id: number, request: BoardRequest): Promise<void> {
    await boardApi.updateBoard(id, request)
  }

  async function deleteBoard(id: number): Promise<void> {
    await boardApi.deleteBoard(id)
  }

  async function createReply(boardId: number, request: ReplyRequest): Promise<void> {
    await boardApi.createReply(boardId, request)
    await fetchBoard(boardId)
  }

  async function updateReply(boardId: number, replyId: number, request: ReplyRequest): Promise<void> {
    await boardApi.updateReply(boardId, replyId, request)
    await fetchBoard(boardId)
  }

  async function deleteReply(boardId: number, replyId: number): Promise<void> {
    await boardApi.deleteReply(boardId, replyId)
    await fetchBoard(boardId)
  }

  return {
    boards,
    currentBoard,
    totalPages,
    currentPage,
    selectedPostType,
    loading,
    fetchBoards,
    fetchBoard,
    createBoard,
    updateBoard,
    deleteBoard,
    createReply,
    updateReply,
    deleteReply,
  }
})
