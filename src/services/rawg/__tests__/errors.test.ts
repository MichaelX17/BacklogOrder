import { RawgApiError, rawgErrorFromStatus, rawgErrorMessage } from '../errors';

describe('rawgErrorFromStatus', () => {
  it('maps 401 to unauthorized', () => {
    const error = rawgErrorFromStatus(401);
    expect(error.type).toBe('unauthorized');
    expect(error.status).toBe(401);
    expect(error).toBeInstanceOf(RawgApiError);
  });

  it('maps 404 to not-found', () => {
    const error = rawgErrorFromStatus(404);
    expect(error.type).toBe('not-found');
    expect(error.status).toBe(404);
  });

  it('maps 429 to rate-limited', () => {
    const error = rawgErrorFromStatus(429);
    expect(error.type).toBe('rate-limited');
    expect(error.status).toBe(429);
  });

  it('maps other statuses to unknown', () => {
    const error = rawgErrorFromStatus(500);
    expect(error.type).toBe('unknown');
    expect(error.status).toBe(500);
  });
});

describe('rawgErrorMessage', () => {
  it('returns a message for every error type', () => {
    const types = [
      'network',
      'unauthorized',
      'rate-limited',
      'not-found',
      'timeout',
      'unknown',
    ] as const;

    for (const type of types) {
      const error = new RawgApiError(type, 'raw');
      expect(rawgErrorMessage(error).length).toBeGreaterThan(0);
    }
  });
});
