import { parseCsv } from '../parseCsv';

describe('parseCsv', () => {
  it('splits and trims comma-separated values', () => {
    expect(parseCsv('RPG, Action, Adventure')).toEqual(['RPG', 'Action', 'Adventure']);
  });

  it('removes empty entries', () => {
    expect(parseCsv('A,,B, ,C')).toEqual(['A', 'B', 'C']);
  });

  it('returns an empty array for blank input', () => {
    expect(parseCsv('')).toEqual([]);
    expect(parseCsv('   ')).toEqual([]);
    expect(parseCsv(',,,')).toEqual([]);
  });

  it('keeps a single value without commas', () => {
    expect(parseCsv('Indie')).toEqual(['Indie']);
  });
});
