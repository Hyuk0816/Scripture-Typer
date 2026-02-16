<script setup lang="ts">
</script>

<template>
  <div class="min-h-screen bg-[#faf9f6] flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md animate-scaleIn">
      <!-- Logo -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-amber-700">Scripture Typer</h1>
        <p class="mt-2 text-sm text-gray-500">성경 필사 통독 애플리케이션</p>
      </div>

      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <slot />
      </div>
    </div>
  </div>
</template>
