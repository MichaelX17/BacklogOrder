import type { HudColors, HudThemeId } from './types';

export type { HudColors, HudThemeId };

export const hudThemes: Record<HudThemeId, { name: string; version: string; colors: HudColors }> = {
  violet: {
    name: 'Violet Arcana',
    version: 'V1 · Default',
    colors: {
      bgFrom: '#07040d',
      bgVia: '#140826',
      bgTo: '#2a0d4a',
      primary: '#ff2bd6',
      secondary: '#22e6ff',
      surface: 'rgba(28, 12, 52, 0.55)',
      text: '#f4ecff',
      muted: '#a79bbf',
      playing: '#ffb020',
      backlog: '#22e6ff',
      completed: '#6dffb0',
      dropped: '#8a7f9e',
    },
  },
  emerald: {
    name: 'Emerald Circuit',
    version: 'V2 · Alternative',
    colors: {
      bgFrom: '#020a07',
      bgVia: '#05211a',
      bgTo: '#0a3d2b',
      primary: '#b6ff3b',
      secondary: '#2df5d0',
      surface: 'rgba(8, 40, 30, 0.55)',
      text: '#ecfff6',
      muted: '#93b8a8',
      playing: '#ffc43d',
      backlog: '#2df5d0',
      completed: '#8cff7a',
      dropped: '#6f8c80',
    },
  },
  crimson: {
    name: 'Crimson Forge',
    version: 'V3 · Alternative',
    colors: {
      bgFrom: '#0b0203',
      bgVia: '#26050b',
      bgTo: '#4a0a14',
      primary: '#ff7a1a',
      secondary: '#ffd23f',
      surface: 'rgba(52, 10, 18, 0.55)',
      text: '#fff1ea',
      muted: '#c4a29b',
      playing: '#ffd23f',
      backlog: '#3fd8ff',
      completed: '#9dff6a',
      dropped: '#8f7470',
    },
  },
};

export function rgba(hex: string, opacity: number): string {
  const cleanHex = hex.replace('#', '');
  if (cleanHex.length !== 6) {
    return hex;
  }

  const value = Number.parseInt(cleanHex, 16);
  const r = (value >> 16) & 255;
  const g = (value >> 8) & 255;
  const b = value & 255;

  return `rgba(${r}, ${g}, ${b}, ${opacity})`;
}

export function withAlpha(hex: string, opacity: number): string {
  const cleanHex = hex.replace('#', '');
  if (cleanHex.length !== 6) {
    return hex;
  }

  const value = Number.parseInt(cleanHex, 16);
  const r = (value >> 16) & 255;
  const g = (value >> 8) & 255;
  const b = value & 255;

  return `rgba(${r}, ${g}, ${b}, ${opacity})`;
}

export function mix(hexA: string, hexB: string, ratioOfA: number): string {
  const cleanA = hexA.replace('#', '');
  const cleanB = hexB.replace('#', '');
  
  if (cleanA.length !== 6 || cleanB.length !== 6) {
    return hexA;
  }
  
  const intA = Number.parseInt(cleanA, 16);
  const intB = Number.parseInt(cleanB, 16);
  
  const rA = (intA >> 16) & 255;
  const gA = (intA >> 8) & 255;
  const bA = intA & 255;
  
  const rB = (intB >> 16) & 255;
  const gB = (intB >> 8) & 255;
  const bB = intB & 255;
  
  const r = Math.round(rA * ratioOfA + rB * (1 - ratioOfA));
  const g = Math.round(gA * ratioOfA + gB * (1 - ratioOfA));
  const b = Math.round(bA * ratioOfA + bB * (1 - ratioOfA));
  
  return `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`;
}
