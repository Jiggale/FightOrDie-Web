<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <header class="fixed top-0 inset-x-0 z-50 glass-panel mx-4 mt-3 px-6 py-3 flex items-center justify-between rounded-2xl">
      <RouterLink to="/" class="flex items-center gap-2 text-brand-400">
        <span class="text-2xl">⚔️</span>
        <span class="font-display text-xl font-bold text-slate-100">Fight Or Die</span>
      </RouterLink>
      <nav class="flex items-center gap-2">
        <RouterLink
          to="/"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-colors hover:bg-white/10"
          active-class="bg-brand-600/30 text-brand-300"
        >🏆 排行榜</RouterLink>
        <RouterLink
          to="/battle"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-colors hover:bg-white/10"
          active-class="bg-brand-600/30 text-brand-300"
        >🗡️ 战斗</RouterLink>
      </nav>
    </header>

    <main class="flex-1 pt-24 pb-10 px-4">
      <RouterView />
    </main>
  </div>
</template>
