export interface ScoreInput {
  metacritic: number | null;
  rating: number | null;
  playtime: number | null;
  inList?: boolean;
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

export function getScoreDisplay(input: ScoreInput): {
  text: string;
  value: number | null;
  isInfinite: boolean;
  source: 'metacritic' | 'rating' | 'none';
} {
  if (input.inList && input.playtime !== null && input.playtime <= 0) {
    return {
      text: '∞',
      value: null,
      isInfinite: true,
      source: input.metacritic !== null ? 'metacritic' : input.rating !== null ? 'rating' : 'none',
    };
  }

  if (input.metacritic !== null) {
    return {
      text: String(input.metacritic),
      value: input.metacritic,
      isInfinite: false,
      source: 'metacritic',
    };
  }

  if (input.rating !== null) {
    const normalized = normalizedRating(null, input.rating);
    return {
      text: input.rating.toFixed(2),
      value: normalized,
      isInfinite: false,
      source: 'rating',
    };
  }

  return {
    text: '—',
    value: null,
    isInfinite: false,
    source: 'none',
  };
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
