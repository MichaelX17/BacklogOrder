export function parseCsv(input: string): string[] {
  return input
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item !== '');
}
