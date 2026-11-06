<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameStore } from '../store/game'

const store = useGameStore()
const route = useRoute()
const router = useRouter()
const localName = ref('')
const error = ref('')

const playerHpPct = computed(() => {
  if (!store.player) return 0
  return Math.max(0, Math.round((store.player.health / store.player.maxHealth) * 100))
})
const monsterHpPct = computed(() => {
  if (!store.monster) return 0
  return Math.max(0, Math.round((store.monster.health / store.monsterMaxHealth) * 100))
})

const isExtinguished = computed(() => store.battleOutcome === 'EXTINGUISHED')
const isVictory = computed(() => store.battleOutcome === 'VICTORY')

const endingLabel = computed(() => {
  switch (store.battleOutcome) {
    case 'VICTORY':
      return '胜利'
    case 'DEFEATED':
      return '战败'
    case 'EXTINGUISHED':
      return '燃尽'
    case 'FLED':
      return '已撤退'
    default:
      return ''
  }
})

const endingIcon = computed(() => {
  switch (store.battleOutcome) {
    case 'VICTORY':
      return '🏆'
    case 'DEFEATED':
      return '💀'
    case 'EXTINGUISHED':
      return '🔥'
    case 'FLED':
      return '🏃'
    default:
      return ''
  }
})

const endingDescription = computed(() => {
  switch (store.battleOutcome) {
    case 'VICTORY':
      return '你击败了 ' + (store.monster?.name ?? '敌人') + '，荣耀归于你的名字。'
    case 'DEFEATED':
      return '你倒下了，但传说仍在继续。'
    case 'EXTINGUISHED':
      return '燃烧耗尽，你化作了灰烬。'
    case 'FLED':
      return '你脱离了战斗，活着离开了这里。'
    default:
      return ''
  }
})

const actions = [
  { index: 1, label: '⚔️ 攻击', cls: 'bg-brand-600 hover:bg-brand-500' },
  { index: 2, label: '🛡️ 防御', cls: 'bg-indigo-600 hover:bg-indigo-500' },
  { index: 3, label: '💚 治疗', cls: 'bg-emerald-600 hover:bg-emerald-500' },
]

onMounted(async () => {
  const name = route.query.name as string | undefined
  if (name && !store.inBattle) {
    await store.start(name)
  }
})

async function beginLocal() {
  const name = localName.value.trim()
  if (!name) {
    error.value = '请先为英雄命名'
    return
  }
  error.value = ''
  await store.start(name)
}

function doAction(index: number) {
  store.action(index)
}

function doFlee() {
  store.flee()
}

function backHome() {
  store.reset()
  router.push('/')
}

async function rematch() {
  if (store.playerName) {
    await store.start(store.playerName)
  } else {
    backHome()
  }
}
</script>

<template>
  <div class="max-w-5xl mx-auto">
    <!-- 未开始：命名面板 -->
    <section v-if="!store.inBattle" class="glass-panel p-10 text-center max-w-md mx-auto animate-float-up">
      <h2 class="text-3xl font-bold text-brand-300 mb-6">🗡️ 准备战斗</h2>
      <input
        v-model="localName"
        type="text"
        maxlength="20"
        placeholder="输入英雄之名"
        class="w-full px-4 py-3 rounded-xl bg-ink-900/60 border border-white/10 text-slate-100 outline-none focus:border-brand-500 transition-colors"
        @keyup.enter="beginLocal"
      />
      <p v-if="error" class="text-brand-400 text-sm mt-2">{{ error }}</p>
      <button class="game-btn mt-5 w-full bg-gradient-to-r from-brand-600 to-brand-500 text-white" @click="beginLocal">
        开始冒险
      </button>
    </section>

    <!-- 战斗界面 -->
    <section v-else class="space-y-6 animate-float-up">
      <!-- 对战区 -->
      <div class="grid md:grid-cols-2 gap-6">
        <!-- 玩家 -->
        <div class="glass-panel p-6">
          <div class="flex items-center gap-4 mb-4">
            <div class="text-5xl">🧝</div>
            <div>
              <div class="text-lg font-bold text-slate-100">{{ store.playerName }}</div>
              <div class="text-xs text-slate-400">Lv.{{ store.player?.level ?? 1 }}</div>
            </div>
          </div>
          <div class="h-4 rounded-full bg-ink-900/70 overflow-hidden border border-white/10">
            <div class="h-full bg-gradient-to-r from-emerald-500 to-emerald-400 transition-all duration-500"
                 :style="{ width: playerHpPct + '%' }"></div>
          </div>
          <div class="flex justify-between text-xs mt-1 text-slate-300">
            <span>HP {{ store.player?.health }}/{{ store.player?.maxHealth }}</span>
            <span>ATK {{ store.player?.attack }} · DEF {{ store.player?.defense }}</span>
          </div>
        </div>

        <!-- 怪物 -->
        <div class="glass-panel p-6">
          <div class="flex items-center gap-4 mb-4">
            <div class="text-5xl">👹</div>
            <div>
              <div class="text-lg font-bold text-brand-300">{{ store.monster?.name }}</div>
              <div class="text-xs text-slate-400">{{ store.monster?.aiType }} · Lv.{{ store.monster?.level }}</div>
            </div>
          </div>
          <div class="h-4 rounded-full bg-ink-900/70 overflow-hidden border border-white/10">
            <div class="h-full bg-gradient-to-r from-brand-600 to-brand-400 transition-all duration-500"
                 :style="{ width: monsterHpPct + '%' }"></div>
          </div>
          <div class="flex justify-between text-xs mt-1 text-slate-300">
            <span>HP {{ store.monster?.health }}/{{ store.monsterMaxHealth }}</span>
            <span>ATK {{ store.monster?.attack }} · DEF {{ store.monster?.defense }}</span>
          </div>
        </div>
      </div>

      <!-- 燃烧与余烬状态 -->
      <div
        class="glass-panel px-5 py-3 flex flex-wrap items-center justify-center gap-x-6 gap-y-1 text-sm"
        :class="isExtinguished ? 'border-brand-500/60' : ''"
      >
        <span :class="store.burning.flame <= 1 ? 'text-brand-400 font-semibold' : 'text-slate-200'">
          🔥 燃烧 {{ store.burning.flame }}/{{ store.burning.maxFlame }}
        </span>
        <span class="text-slate-400">每回合 -{{ store.burning.burnPerTurn }}</span>
        <span class="text-amber-300">余烬 {{ store.burning.ember }}</span>
        <span v-if="!store.combatEnded" class="text-slate-500">胜利后 +5 余烬</span>
        <span v-else class="text-slate-500">战斗已结束</span>
      </div>

      <!-- 行动按钮 -->
      <div class="glass-panel p-5 flex flex-wrap gap-3 justify-center">
        <button
          v-for="a in actions"
          :key="a.index"
          class="game-btn text-white"
          :class="a.cls"
          :disabled="store.loading || store.combatEnded"
          @click="doAction(a.index)"
        >{{ a.label }}</button>
        <button
          class="game-btn bg-slate-700 hover:bg-slate-600 text-white"
          :disabled="store.loading || store.combatEnded"
          @click="doFlee"
        >🏃 逃跑</button>
      </div>

      <!-- 战斗日志 -->
      <div class="glass-panel p-5">
        <h3 class="text-sm text-slate-400 mb-2">📜 战斗日志 · 第 {{ store.turn }} 回合</h3>
        <div class="h-48 overflow-y-auto space-y-1 text-sm">
          <p v-for="(line, i) in store.battleLog" :key="i" class="text-slate-300 animate-float-up">{{ line }}</p>
        </div>
      </div>
    </section>

    <!-- 结算横幅 -->
    <div
      v-if="store.combatEnded"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm"
    >
      <div class="glass-panel p-10 text-center max-w-md animate-float-up">
        <div class="text-6xl mb-4">{{ endingIcon }}</div>
        <h2 class="text-3xl font-bold mb-2" :class="isVictory ? 'text-emerald-400' : 'text-brand-400'">
          {{ endingLabel }}
        </h2>
        <p class="text-slate-300 mb-4">{{ endingDescription }}</p>
        <div class="mb-6 space-y-1 text-sm">
          <p v-if="isVictory" class="text-amber-300">获得余烬 +5</p>
          <p class="text-slate-400">当前燃烧 {{ store.burning.flame }}/{{ store.burning.maxFlame }}</p>
          <p class="text-slate-400">当前余烬 {{ store.burning.ember }}</p>
        </div>
        <div class="flex gap-3 justify-center">
          <button class="game-btn bg-brand-600 hover:bg-brand-500 text-white" @click="rematch">⚔️ 再战</button>
          <button class="game-btn bg-slate-700 hover:bg-slate-600 text-white" @click="backHome">🏆 排行榜</button>
        </div>
      </div>
    </div>
  </div>
</template>
