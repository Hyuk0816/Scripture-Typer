<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBoardStore } from '@/stores/board'
import Spinner from '@/components/atoms/Spinner.vue'
import type { PostType } from '@/types/board'

const router = useRouter()
const boardStore = useBoardStore()

type TabId = 'ALL' | PostType
const tabs: { id: TabId; label: string }[] = [
  { id: 'ALL', label: '전체' },
  { id: 'NOTICE', label: '공지' },
  { id: 'BIBLE_QUESTION', label: '성경질문' },
  { id: 'FREE', label: '자유' },
  { id: 'SUGGESTION', label: '제안' },
]

const activeTab = computed(() => boardStore.selectedPostType ?? 'ALL')

function selectTab(tabId: TabId) {
  const postType = tabId === 'ALL' ? undefined : tabId
  boardStore.fetchBoards(0, 10, postType)
}

function goToPage(page: number) {
  boardStore.fetchBoards(page, 10, boardStore.selectedPostType)
}

function postTypeBadgeClass(postType: string) {
  switch (postType) {
    case 'NOTICE':
      return 'bg-red-100 text-red-700'
    case 'BIBLE_QUESTION':
      return 'bg-amber-100 text-amber-700'
    case 'FREE':
      return 'bg-blue-100 text-blue-700'
    case 'SUGGESTION':
      return 'bg-green-100 text-green-700'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}

function postTypeLabel(postType: string) {
  switch (postType) {
    case 'NOTICE':
      return '공지'
    case 'BIBLE_QUESTION':
      return '성경질문'
    case 'FREE':
      return '자유'
    case 'SUGGESTION':
      return '제안'
    default:
      return postType
  }
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}.${day}`
}

onMounted(() => {
  boardStore.fetchBoards()
})
</script>

<template>
  <div class="max-w-3xl mx-auto">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6 mt-2 gap-3">
      <div class="min-w-0">
        <h1 class="text-xl sm:text-2xl font-bold text-gray-800">게시판</h1>
        <p class="text-xs sm:text-sm text-gray-500 mt-1">자유롭게 소통하고 질문하세요</p>
      </div>
      <button
        @click="router.push({ name: 'board-create' })"
        class="px-4 py-2 bg-amber-600 text-white text-sm font-medium rounded-lg hover:bg-amber-700 transition-colors"
      >
        글쓰기
      </button>
    </div>

    <!-- Category Tabs -->
    <div class="flex gap-1 bg-gray-100 rounded-lg p-1 mb-6 overflow-x-auto">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="selectTab(tab.id)"
        class="flex-1 py-2 text-sm font-medium rounded-md transition-colors whitespace-nowrap min-w-0"
        :class="activeTab === tab.id ? 'bg-white text-amber-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Loading -->
    <div v-if="boardStore.loading" class="flex justify-center py-12">
      <Spinner />
    </div>

    <!-- Empty -->
    <div
      v-else-if="boardStore.boards.length === 0"
      class="bg-white rounded-xl shadow-sm border border-gray-100 p-10 text-center"
    >
      <div class="text-4xl mb-4">📝</div>
      <h3 class="text-lg font-semibold text-gray-700 mb-2">아직 게시글이 없습니다</h3>
      <p class="text-sm text-gray-500">첫 번째 글을 작성해 보세요.</p>
    </div>

    <!-- Board List -->
    <div v-else class="space-y-2">
      <button
        v-for="post in boardStore.boards"
        :key="post.id"
        @click="router.push({ name: 'board-detail', params: { id: post.id } })"
        :class="[
          'w-full rounded-xl shadow-sm p-4 text-left hover:shadow-md transition-all',
          post.postType === 'NOTICE'
            ? 'bg-red-50 border border-red-200 hover:border-red-300'
            : 'bg-white border border-gray-100 hover:border-gray-200',
        ]"
      >
        <div class="flex items-center gap-2 mb-1">
          <span
            class="text-[10px] px-2 py-0.5 rounded-full font-medium"
            :class="postTypeBadgeClass(post.postType)"
          >
            {{ postTypeLabel(post.postType) }}
          </span>
          <h3 class="font-semibold text-gray-800 truncate flex-1">{{ post.title }}</h3>
        </div>
        <div class="flex items-center gap-3 text-xs text-gray-400 mt-2">
          <span>
            <span v-if="post.authorRole === 'PASTOR'" class="text-blue-600 font-medium">[교역자] </span>{{ post.authorName }}
          </span>
          <span>{{ formatDate(post.createdAt) }}</span>
          <span v-if="post.replyCount > 0" class="text-amber-600 font-medium">
            답글 {{ post.replyCount }}
          </span>
        </div>
      </button>
    </div>

    <!-- Pagination -->
    <div
      v-if="!boardStore.loading && boardStore.totalPages > 1"
      class="flex justify-center gap-1 mt-6"
    >
      <button
        v-for="page in boardStore.totalPages"
        :key="page"
        @click="goToPage(page - 1)"
        class="w-8 h-8 text-sm rounded-md transition-colors"
        :class="
          boardStore.currentPage === page - 1
            ? 'bg-amber-600 text-white'
            : 'text-gray-500 hover:bg-gray-100'
        "
      >
        {{ page }}
      </button>
    </div>
  </div>
</template>
