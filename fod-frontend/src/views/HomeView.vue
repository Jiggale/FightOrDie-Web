<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../store/game'

const store = useGameStore()
const router = useRouter()
const playerName = ref('')
const submitting = ref(false)

onMounted(() => {
  store.loadLeaderboard()
})

function rankBadge(rank: number): string {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return String(rank)
}

async function startGame() {
  const name = playerName.value.trim() || '无名英雄'
  submitting.value = true
  router.push({ path: '/battle', query: { name } })
}
</script>

<template>
  <div class="max-w-5xl mx-auto space-y-8">
    <!-- Hero -->
    <section class="glass-panel p-10 text-center animate-float-up">
      <h1 class="text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-brand-400 to-indigo-400">
        FIGHT OR DIE
      </h1>
      <p class="mt-4 text-slate-300 max-w-2xl mx-auto">
        一个基于 Spring Boot + MyBatis + MySQL 全栈打造的策略回合制 RPG。
        创建你的英雄，与深渊怪物展开生死决斗，每一次战斗都将铭刻于金币排行榜。
      </p>
    </section>

    <div class="grid md:grid-cols-2 gap-8">
      <!-- 创建角色 -->
      <section class="glass-panel p-8 animate-float-up">
        <h2 class="text-2xl font-bold text-brand-300 mb-4">🧙 创建英雄</h2>
        <label class="block text-sm text-slate-400 mb-2">英雄之名</label>
        <input
          v-model="playerName"
          type="text"
          maxlength="20"
          placeholder="为你的角色命名"
          class="w-full px-4 py-3 rounded-xl bg-ink-900/60 border border-white/10 text-slate-100 outline-none focus:border-brand-500 transition-colors"
          @keyup.enter="startGame"
        />
        <button
          class="game-btn mt-5 w-full bg-gradient-to-r from-brand-600 to-brand-500 text-white animate-pulse-glow"
          :disabled="submitting"
          @click="startGame"
        >
          ⚔️ 进入战斗
        </button>
      </section>

      <!-- 排行榜 -->
      <section class="glass-panel p-8 animate-float-up">
        <h2 class="text-2xl font-bold text-amber-300 mb-4">🏆 金币排行榜</h2>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="text-slate-400 border-b border-white/10">
                <th class="text-left py-2">排名</th>
                <th class="text-left py-2">英雄</th>
                <th class="text-right py-2">胜场</th>
                <th class="text-right py-2">金币</th>
                <th class="text-right py-2">等级</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(p, i) in store.leaderboard"
                :key="p.playerName"
                class="border-b border-white/5 hover:bg-white/5 transition-colors"
              >
                <td class="py-2">{{ rankBadge(i + 1) }}</td>
                <td class="py-2 font-medium text-slate-100">{{ p.playerName }}</td>
                <td class="py-2 text-right text-emerald-400">{{ p.totalWins }}</td>
                <td class="py-2 text-right text-amber-400">{{ p.totalGold }}</td>
                <td class="py-2 text-right text-indigo-300">{{ p.highestLevel }}</td>
              </tr>
              <tr v-if="store.leaderboard.length === 0">
                <td colspan="5" class="py-6 text-center text-slate-500">暂无战绩，快去战斗吧！</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>
