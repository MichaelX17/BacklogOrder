import { computeScore, normalizedRating } from '../score';

describe('normalizedRating', () => {
  it('returns metacritic when present', () => {
    expect(normalizedRating(90, 4.5)).toBe(90);
  });

  it('prefers metacritic over rating', () => {
    expect(normalizedRating(80, 5)).toBe(80);
  });

  it('falls back to rating multiplied by 20', () => {
    expect(normalizedRating(null, 4.5)).toBe(90);
  });

  it('returns null when both sources are missing', () => {
    expect(normalizedRating(null, null)).toBeNull();
  });
});

describe('computeScore', () => {
  it('divides metacritic by playtime', () => {
    expect(computeScore({ metacritic: 90, rating: 4.5, playtime: 30 })).toBe(3);
  });

  it('uses rating multiplied by 20 when metacritic is missing', () => {
    expect(computeScore({ metacritic: null, rating: 4, playtime: 20 })).toBe(4);
  });

  it('returns null when both rating sources are missing', () => {
    expect(computeScore({ metacritic: null, rating: null, playtime: 10 })).toBeNull();
  });

  it('returns null when playtime is 0', () => {
    expect(computeScore({ metacritic: 90, rating: null, playtime: 0 })).toBeNull();
  });

  it('returns null when playtime is null', () => {
    expect(computeScore({ metacritic: 90, rating: null, playtime: null })).toBeNull();
  });

  it('returns null when playtime is negative', () => {
    expect(computeScore({ metacritic: 90, rating: null, playtime: -5 })).toBeNull();
  });

  it('returns 0 when metacritic is 0 and playtime is positive', () => {
    expect(computeScore({ metacritic: 0, rating: 5, playtime: 10 })).toBe(0);
  });

  it('returns a fractional score', () => {
    expect(computeScore({ metacritic: 75, rating: null, playtime: 40 })).toBe(1.875);
  });
});
