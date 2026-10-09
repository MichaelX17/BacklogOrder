export type GameStatus = 'backlog' | 'playing' | 'completed' | 'dropped'

export type Game = {
  id: string
  name: string
  cover: string
  metacritic: number | null
  rating: number | null
  playtime: number
  franchise?: string
  franchiseOrder?: number
  status: GameStatus
}

export const statusLabels: Record<GameStatus, string> = {
  backlog: 'Backlog',
  playing: 'Playing',
  completed: 'Completed',
  dropped: 'Dropped',
}

export const games: Game[] = [
  {
    id: 'metro-2033',
    name: 'Metro 2033',
    cover: '/covers/metro-2033.png',
    metacritic: 81,
    rating: 4.0,
    playtime: 10,
    franchise: 'Metro',
    franchiseOrder: 1,
    status: 'completed',
  },
  {
    id: 'metro-last-light',
    name: 'Metro: Last Light',
    cover: '/covers/metro-last-light.png',
    metacritic: 82,
    rating: 4.2,
    playtime: 9,
    franchise: 'Metro',
    franchiseOrder: 2,
    status: 'backlog',
  },
  {
    id: 'tormented-souls',
    name: 'Tormented Souls',
    cover: '/covers/tormented-souls.png',
    metacritic: null,
    rating: 3.6,
    playtime: 8,
    status: 'dropped',
  },
  {
    id: 'devil-may-cry-5',
    name: 'Devil May Cry 5',
    cover: '/covers/devil-may-cry-5.png',
    metacritic: 89,
    rating: 4.4,
    playtime: 12,
    status: 'playing',
  },
  {
    id: 'resident-evil-4',
    name: 'Resident Evil 4 (2005)',
    cover: '/covers/resident-evil-4.png',
    metacritic: 96,
    rating: 4.6,
    playtime: 15,
    status: 'backlog',
  },
]

export type HudTheme = {
  id: 'violet' | 'emerald' | 'crimson'
  version: string
  name: string
  className: string
  swatches: string[]
}

export const hudThemes: HudTheme[] = [
  {
    id: 'violet',
    version: 'V1 · Default',
    name: 'Violet Arcana',
    className: 'theme-violet',
    swatches: ['#2a0d4a', '#ff2bd6', '#22e6ff'],
  },
  {
    id: 'emerald',
    version: 'V2 · Alternative',
    name: 'Emerald Circuit',
    className: 'theme-emerald',
    swatches: ['#0a3d2b', '#b6ff3b', '#2df5d0'],
  },
  {
    id: 'crimson',
    version: 'V3 · Alternative',
    name: 'Crimson Forge',
    className: 'theme-crimson',
    swatches: ['#4a0a14', '#ff7a1a', '#ffd23f'],
  },
]
