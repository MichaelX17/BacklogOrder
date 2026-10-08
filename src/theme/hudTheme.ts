export const HUD_THEME = {
  bgFrom: '#07040d',
  bgVia: '#140826',
  bgTo: '#2a0d4a',
  primary: '#ff2bd6',
  secondary: '#22e6ff',
  surface: 'rgba(28, 12, 52, 0.55)',
  panel: 'rgba(11, 9, 16, 0.82)',
  text: '#f4ecff',
  muted: '#a79bbf',
  playing: '#ffb020',
  backlog: '#22e6ff',
  border: 'rgba(34, 230, 255, 0.28)',
  shadow: 'rgba(255, 43, 214, 0.28)',
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
