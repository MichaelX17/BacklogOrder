export interface ScoreInput {
  metacritic: number | null;
  rating: number | null;
  playtime: number | null;
}

export function normalizedRating(metacritic: number | null, rating: number | null): number | null {
  if (metacritic !== null) {
    return metacritic;
  }
  if (rating !== null) {
    return rating * 20;
  }
  return null;
}

export function computeScore(input: ScoreInput): number | null {
  const normalized = normalizedRating(input.metacritic, input.rating);
  if (normalized === null) {
    return null;
  }
  if (input.playtime === null || input.playtime <= 0) {
    return null;
  }
  return normalized / input.playtime;
}
