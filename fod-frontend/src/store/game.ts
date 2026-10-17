import { defineStore } from 'pinia'
import {
  startBattle,
  battleAction,
  fleeBattle,
  getLeaderboard,
  type BattleCreated,
  type BattleState,
  type PlayerStats,
} from '../api/game'

export const useGameStore = defineStore('game', {
  state: () => ({
    sessionId: '' as string,
    playerName: '' as string,
    player: null as BattleState['player'] | null,
    monster: null as BattleState['monster'] | null,
    monsterMaxHealth: 1 as number,
    strategies: {} as Record<number, string>,
    battleLog: [] as string[],
    combatEnded: false,
    playerWon: false,
    turn: 0,
    loading: false,
    inBattle: false,
    leaderboard: [] as PlayerStats[],
  }),

  actions: {
    async start(playerName: string) {
      this.loading = true
      try {
        const created: BattleCreated = await startBattle(playerName)
        this.sessionId = created.sessionId
        this.playerName = playerName
        this.strategies = created.strategies
        this.monster = created.monster
        this.monsterMaxHealth = created.monster.health || 1
        this.player = null
        this.battleLog = ['遭遇 ' + created.monster.name + '！战斗开始！']
        this.combatEnded = false
        this.playerWon = false
        this.turn = 0
        this.inBattle = true
      } finally {
        this.loading = false
      }
    },

    async action(strategyIndex: number) {
      if (!this.sessionId || this.combatEnded) return
      this.loading = true
      try {
        const state: BattleState = await battleAction(this.sessionId, strategyIndex)
        this.applyState(state)
      } finally {
        this.loading = false
      }
    },

    async flee() {
      if (!this.sessionId || this.combatEnded) return
      this.loading = true
      try {
        const state: BattleState = await fleeBattle(this.sessionId)
        this.applyState(state)
      } finally {
        this.loading = false
      }
    },

    applyState(state: BattleState) {
      this.player = state.player
      this.monster = state.monster
      this.combatEnded = state.combatEnded
      this.playerWon = state.playerWon
      this.turn = state.turn
      const newLines = state.log.slice(-5)
      this.battleLog = [...this.battleLog, ...newLines].slice(-50)
    },

    async loadLeaderboard() {
      this.leaderboard = await getLeaderboard(20)
    },

    reset() {
      this.inBattle = false
      this.sessionId = ''
      this.player = null
      this.monster = null
      this.battleLog = []
    },
  },
})
