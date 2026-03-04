<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBoardStore } from '@/stores/board'
import { useAuthStore } from '@/stores/auth'
import Spinner from '@/components/atoms/Spinner.vue'

const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()
const authStore = useAuthStore()

const boardId = computed(() => Number(route.params.id))
const board = computed(() => boardStore.currentBoard)
const forbidden = ref(false)

const replyContent = ref('')
const editingReplyId = ref<number | null>(null)
const editingReplyContent = ref('')

const isAuthor = computed(() => board.value?.authorId === authStore.user?.id)
const isAdmin = computed(() => authStore.user?.role === 'ADMIN')
const canModify = computed(() => isAuthor.value || isAdmin.value)

const canReply = computed(() => {
  if (!board.value) return false
  if (board.value.postType === 'NOTICE') return false
  if (board.value.postType !== 'BIBLE_QUESTION') return true
  const role = authStore.user?.role
  return role === 'PASTOR' || role === 'MOKJANG' || role === 'ADMIN'
})

function postTypeBadgeClass(postType: string) {
  switch (postType) {
    case 'NOTICE': return 'bg-red-100 text-red-700'
    case 'BIBLE_QUESTION': return 'bg-amber-100 text-amber-700'
    case 'FREE': return 'bg-blue-100 text-blue-700'
    case 'SUGGESTION': return 'bg-green-100 text-green-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}

function postTypeLabel(postType: string) {
  switch (postType) {
    case 'NOTICE': return '공지'
    case 'BIBLE_QUESTION': return '성경질문'
    case 'FREE': return '자유'
    case 'SUGGESTION': return '제안'
    default: return postType
  }
}

function roleLabel(role: string) {
  switch (role) {
    case 'ADMIN': return '관리자'
    case 'PASTOR': return '목사'
    case 'MOKJANG': return '목장'
    default: return ''
  }
}

function formatDateTime(dateStr: string) {
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}.${m}.${d} ${h}:${min}`
}

async function handleDelete() {
  if (!confirm('게시글을 삭제하시겠습니까?')) return
  await boardStore.deleteBoard(boardId.value)
  router.push({ name: 'board-list' })
}

async function submitReply() {
  if (!replyContent.value.trim()) return
  await boardStore.createReply(boardId.value, { content: replyContent.value })
  replyContent.value = ''
}

function startEditReply(replyId: number, content: string) {
  editingReplyId.value = replyId
  editingReplyContent.value = content
}

function cancelEditReply() {
  editingReplyId.value = null
  editingReplyContent.value = ''
}

async function submitEditReply(replyId: number) {
  if (!editingReplyContent.value.trim()) return
  await boardStore.updateReply(boardId.value, replyId, { content: editingReplyContent.value })
  editingReplyId.value = null
  editingReplyContent.value = ''
}

async function handleDeleteReply(replyId: number) {
  if (!confirm('답글을 삭제하시겠습니까?')) return
  await boardStore.deleteReply(boardId.value, replyId)
}

onMounted(async () => {
  try {
    await boardStore.fetchBoard(boardId.value)
  } catch (err: unknown) {
    const error = err as { response?: { status?: number } }
    if (error.response?.status === 403) {
      forbidden.value = true
    }
  }
})
</script>

<template>
  <div class="max-w-3xl mx-auto">
    <!-- Loading -->
    <div v-if="boardStore.loading" class="flex justify-center py-12">
      <Spinner />
    </div>

    <!-- Forbidden -->
    <div v-else-if="forbidden" class="bg-white rounded-xl shadow-sm border border-gray-100 p-10 text-center mt-4">
      <div class="text-4xl mb-4">🔒</div>
      <h3 class="text-lg font-semibold text-gray-700 mb-2">접근 권한이 없습니다</h3>
      <p class="text-sm text-gray-500 mb-4">이 게시글은 열람 권한이 제한되어 있습니다.</p>
      <button
        @click="router.push({ name: 'board-list' })"
        class="text-sm text-amber-600 hover:text-amber-700 font-medium"
      >
        목록으로 돌아가기
      </button>
    </div>

    <!-- Board Detail -->
    <template v-else-if="board">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mt-2">
        <!-- Header -->
        <div class="flex items-center gap-2 mb-3">
          <span
            class="text-[10px] px-2 py-0.5 rounded-full font-medium"
            :class="postTypeBadgeClass(board.postType)"
          >
            {{ postTypeLabel(board.postType) }}
          </span>
        </div>
        <h1 class="text-xl font-bold text-gray-800 mb-3">{{ board.title }}</h1>
        <div class="flex items-center gap-3 text-sm text-gray-500 mb-4 pb-4 border-b border-gray-100">
          <span class="font-medium text-gray-700">{{ board.authorName }}</span>
          <span
            v-if="roleLabel(board.authorRole)"
            class="text-[10px] px-1.5 py-0.5 rounded bg-gray-100 text-gray-500"
          >
            {{ roleLabel(board.authorRole) }}
          </span>
          <span>{{ formatDateTime(board.createdAt) }}</span>
        </div>

        <!-- Content -->
        <div class="text-gray-700 leading-relaxed whitespace-pre-wrap min-h-[100px]">
          {{ board.content }}
        </div>

        <!-- Actions -->
        <div v-if="canModify" class="flex gap-2 mt-6 pt-4 border-t border-gray-100">
          <button
            @click="router.push({ name: 'board-edit', params: { id: board.id } })"
            class="px-3 py-1.5 text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >
            수정
          </button>
          <button
            @click="handleDelete"
            class="px-3 py-1.5 text-sm text-red-500 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
          >
            삭제
          </button>
        </div>
      </div>

      <!-- Replies Section -->
      <div class="mt-6">
        <h2 class="text-sm font-semibold text-gray-700 mb-4">
          답글 {{ board.replies.length }}개
        </h2>

        <!-- Reply List -->
        <div class="space-y-3">
          <div
            v-for="reply in board.replies"
            :key="reply.id"
            class="bg-white rounded-xl shadow-sm border border-gray-100 p-4"
          >
            <div class="flex items-center gap-2 mb-2">
              <span class="text-sm font-medium text-gray-700">{{ reply.authorName }}</span>
              <span
                v-if="roleLabel(reply.authorRole)"
                class="text-[10px] px-1.5 py-0.5 rounded bg-gray-100 text-gray-500"
              >
                {{ roleLabel(reply.authorRole) }}
              </span>
              <span class="text-xs text-gray-400">{{ formatDateTime(reply.createdAt) }}</span>
            </div>

            <!-- Edit mode -->
            <div v-if="editingReplyId === reply.id">
              <textarea
                v-model="editingReplyContent"
                rows="3"
                class="w-full border border-gray-200 rounded-lg p-3 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-transparent resize-none"
              />
              <div class="flex gap-2 mt-2">
                <button
                  @click="submitEditReply(reply.id)"
                  class="px-3 py-1 text-sm bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors"
                >
                  저장
                </button>
                <button
                  @click="cancelEditReply"
                  class="px-3 py-1 text-sm text-gray-500 hover:text-gray-700 transition-colors"
                >
                  취소
                </button>
              </div>
            </div>

            <!-- View mode -->
            <template v-else>
              <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ reply.content }}</p>
              <div
                v-if="reply.authorId === authStore.user?.id || isAdmin"
                class="flex gap-2 mt-2"
              >
                <button
                  v-if="reply.authorId === authStore.user?.id"
                  @click="startEditReply(reply.id, reply.content)"
                  class="text-xs text-gray-400 hover:text-gray-600 transition-colors"
                >
                  수정
                </button>
                <button
                  @click="handleDeleteReply(reply.id)"
                  class="text-xs text-gray-400 hover:text-red-500 transition-colors"
                >
                  삭제
                </button>
              </div>
            </template>
          </div>
        </div>

        <!-- Reply Input -->
        <div v-if="canReply" class="mt-4">
          <textarea
            v-model="replyContent"
            rows="3"
            placeholder="답글을 입력하세요..."
            class="w-full border border-gray-200 rounded-lg p-3 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-transparent resize-none"
          />
          <div class="flex justify-end mt-2">
            <button
              @click="submitReply"
              :disabled="!replyContent.trim()"
              class="px-4 py-2 bg-amber-600 text-white text-sm font-medium rounded-lg hover:bg-amber-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              답글 작성
            </button>
          </div>
        </div>
        <div
          v-else-if="board.postType === 'NOTICE'"
          class="mt-4 text-center text-sm text-gray-400 py-4"
        >
          공지글에는 답글을 작성할 수 없습니다.
        </div>
        <div
          v-else-if="board.postType === 'BIBLE_QUESTION'"
          class="mt-4 text-center text-sm text-gray-400 py-4"
        >
          성경질문 답글은 목사/목장/관리자만 작성할 수 있습니다.
        </div>
      </div>

      <!-- Back button -->
      <div class="mt-6">
        <button
          @click="router.push({ name: 'board-list' })"
          class="text-sm text-gray-500 hover:text-gray-700 transition-colors"
        >
          &larr; 목록으로
        </button>
      </div>
    </template>
  </div>
</template>
