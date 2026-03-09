import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { logApi } from '@/utils/api'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiresAdmin?: boolean
    guest?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/components/templates/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/pages/DashboardPage.vue'),
        },
        {
          path: 'reading/:book/:chapter',
          name: 'reading',
          component: () => import('@/pages/ReadingPage.vue'),
        },
        {
          path: 'typing/:book/:chapter',
          name: 'typing',
          component: () => import('@/pages/TypingPage.vue'),
        },
        {
          path: 'mypage',
          name: 'mypage',
          component: () => import('@/pages/MyPagePage.vue'),
        },
        {
          path: 'board',
          name: 'board-list',
          component: () => import('@/pages/BoardListPage.vue'),
        },
        {
          path: 'board/create',
          name: 'board-create',
          component: () => import('@/pages/BoardWritePage.vue'),
        },
        {
          path: 'board/:id',
          name: 'board-detail',
          component: () => import('@/pages/BoardDetailPage.vue'),
        },
        {
          path: 'board/:id/edit',
          name: 'board-edit',
          component: () => import('@/pages/BoardWritePage.vue'),
        },
        {
          path: 'ranking',
          name: 'ranking',
          component: () => import('@/pages/RankingPage.vue'),
        },
        {
          path: 'group',
          name: 'group',
          component: () => import('@/pages/GroupReadingPage.vue'),
        },
        {
          path: 'group/:planId',
          name: 'group-detail',
          component: () => import('@/pages/GroupPlanDetailPage.vue'),
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/LoginPage.vue'),
      meta: { guest: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('@/pages/SignupPage.vue'),
      meta: { guest: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/pages/AdminUsersPage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/stats',
      name: 'admin-stats',
      component: () => import('@/pages/AdminStatisticsPage.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guest && authStore.isAuthenticated) {
    return '/'
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return '/'
  }
})

router.afterEach((to) => {
  const authStore = useAuthStore()
  if (authStore.isAuthenticated && to.name) {
    logApi.logMenuAccess({ menuName: String(to.name), path: to.fullPath })
  }
})

export default router