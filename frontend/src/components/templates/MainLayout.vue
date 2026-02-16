<script setup lang="ts">
import Sidebar from '@/components/organisms/Sidebar.vue'
import AppHeader from '@/components/organisms/AppHeader.vue'
</script>

<template>
  <div class="min-h-screen bg-[#faf9f6] flex">
    <Sidebar />
    <div class="flex-1 flex flex-col min-w-0">
      <AppHeader />
      <main class="flex-1 overflow-y-auto p-6 md:p-8">
        <router-view />
      </main>
    </div>
  </div>
</template>