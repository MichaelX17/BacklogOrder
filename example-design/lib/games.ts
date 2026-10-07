export type GameStatus = 'playing' | 'backlog'

export type Game = {
  id: string
  title: string
  cover: string
  playtimeHours: number
  metacritic: number
  status: GameStatus
  price: number
}

export const games: Game[] = [
  {
    id: 'metro-2033',
    title: 'Metro 2033',
    cover: '/covers/metro-2033.png',
    playtimeHours: 10,
    metacritic: 81,
    status: 'playing',
    price: 39.5,
  },
  {
    id: 'tormented-souls',
    title: 'Tormented Souls',
    cover: '/covers/tormented-souls.png',
    playtimeHours: 8,
    metacritic: 72,
    status: 'backlog',
    price: 19.0,
  },
  {
    id: 'resident-evil-4',
    title: 'Resident Evil 4 (2005)',
    cover: '/covers/resident-evil-4.png',
    playtimeHours: 15,
    metacritic: 96,
    status: 'backlog',
    price: 16.6,
  },
  {
    id: 'devil-may-cry-5',
    title: 'Devil May Cry 5',
    cover: '/covers/devil-may-cry-5.png',
    playtimeHours: 12,
    metacritic: 89,
    status: 'playing',
    price: 9.78,
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
