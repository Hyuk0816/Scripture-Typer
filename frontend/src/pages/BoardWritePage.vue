<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBoardStore } from '@/stores/board'
import { useAuthStore } from '@/stores/auth'
import type { PostType, BoardRequest } from '@/types/board'
import Spinner from '@/components/atoms/Spinner.vue'

const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()
const authStore = useAuthStore()

const boardId = computed(() => route.params.id ? Number(route.params.id) : null)
const isEditMode = computed(() => boardId.value !== null)

const postType = ref<PostType>('FREE')
const title = ref('')
const content = ref('')
const loading = ref(false)
const error = ref('')

const postTypeOptions = computed(() => {
  const options: { value: PostType; label: string }[] = [
    { value: 'BIBLE_QUESTION', label: '성경질문' },
    { value: 'FREE', label: '자유' },
    { value: 'SUGGESTION', label: '제안' },
  ]
  if (authStore.isAdmin) {
    options.unshift({ value: 'NOTICE', label: '공지' })
  }
  return options
})

const isValid = computed(() => title.value.trim() && content.value.trim())

async function handleSubmit() {
  if (!isValid.value) return
  error.value = ''
  loading.value = true

  const request: BoardRequest = {
    postType: postType.value,
    title: title.value.trim(),
    content: content.value.trim(),
  }

  try {
    if (isEditMode.value) {
      await boardStore.updateBoard(boardId.value!, request)
      router.push({ name: 'board-detail', params: { id: boardId.value! } })
    } else {
      await boardStore.createBoard(request)
      router.push({ name: 'board-list' })
    }
  } catch (err: unknown) {
    const e = err as { response?: { data?: { message?: string } } }
    error.value = e.response?.data?.message || '저장에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (isEditMode.value) {
    loading.value = true
    try {
      await boardStore.fetchBoard(boardId.value!)
      if (boardStore.currentBoard) {
        postType.value = boardStore.currentBoard.postType
        title.value = boardStore.currentBoard.title
        content.value = boardStore.currentBoard.content
      }
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <div class="max-w-3xl mx-auto">
    <div class="mb-6 mt-2">
      <h1 class="text-2xl font-bold text-gray-800">
        {{ isEditMode ? '게시글 수정' : '게시글 작성' }}
      </h1>
    </div>

    <!-- Loading for edit mode -->
    <div v-if="loading && isEditMode && !title" class="flex justify-center py-12">
      <Spinner />
    </div>

    <form v-else @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Post Type -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
        <select
          v-model="postType"
          class="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-700 bg-white focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-transparent"
        >
          <option v-for="opt in postTypeOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <!-- Title -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">제목</label>
        <input
          v-model="title"
          type="text"
          placeholder="제목을 입력하세요"
          class="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-transparent"
        />
      </div>

      <!-- Content -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">내용</label>
        <textarea
          v-model="content"
          rows="12"
          placeholder="내용을 입력하세요"
          class="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-transparent resize-none"
        />
      </div>

      <!-- Error -->
      <p v-if="error" class="text-sm text-red-500">{{ error }}</p>

      <!-- Buttons -->
      <div class="flex gap-3 pt-2">
        <button
          type="submit"
          :disabled="!isValid || loading"
          class="px-6 py-2.5 bg-amber-600 text-white text-sm font-medium rounded-lg hover:bg-amber-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ loading ? '저장 중...' : (isEditMode ? '수정' : '작성') }}
        </button>
        <button
          type="button"
          @click="router.back()"
          class="px-6 py-2.5 text-sm text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
        >
          취소
        </button>
      </div>
    </form>
  </div>
</template>
