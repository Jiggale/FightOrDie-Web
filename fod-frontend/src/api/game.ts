import request, { ensureToken } from './request'

export interface Player {
  name: string
  level: number
  health: number
  maxHealth: number
  attack: number
  defense: number
  gold: number
  exp: number
}

export interface Monster {
  name: string
  level: number
  health: number
  attack: number
  defense: number
  aiType: string
}

export interface BattleState {
  player: Player
  monster: Monster
  combatEnded: boolean
  playerWon: boolean
  turn: number
  log: string[]
}

export interface BattleCreated {
  sessionId: string
  monster: Monster
  strategies: Record<number, string>
}

export interface PlayerStats {
  playerName: string
  totalBattles: number
  totalWins: number
  totalExp: number
  totalGold: number
  monstersKilled: number
  highestLevel: number
}

export async function startBattle(playerName: string): Promise<BattleCreated> {
  await ensureToken()
  return request.post('/battle/start', { playerName }) as Promise<BattleCreated>
}

export async function battleAction(
  sessionId: string,
  strategyIndex: number,
): Promise<BattleState> {
  await ensureToken()
  return request.post(`/battle/${sessionId}/action`, { strategyIndex }) as Promise<BattleState>
}

export async function fleeBattle(sessionId: string): Promise<BattleState> {
  await ensureToken()
  return request.post(`/battle/${sessionId}/flee`) as Promise<BattleState>
}

export async function getStrategies(): Promise<Record<number, string>> {
  await ensureToken()
  return request.get('/strategies') as Promise<Record<number, string>>
}

export async function getLeaderboard(limit = 20): Promise<PlayerStats[]> {
  await ensureToken()
  return request.get(`/stats/leaderboard?limit=${limit}`) as Promise<PlayerStats[]>
}

export async function getPlayerStats(name: string): Promise<PlayerStats> {
  await ensureToken()
  return request.get(`/stats/player/${encodeURIComponent(name)}`) as Promise<PlayerStats>
}
