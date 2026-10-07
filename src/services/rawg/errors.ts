export type RawgErrorType =
  'network' | 'unauthorized' | 'rate-limited' | 'not-found' | 'timeout' | 'unknown';

export class RawgApiError extends Error {
  readonly type: RawgErrorType;
  readonly status: number | null;

  constructor(type: RawgErrorType, message: string, status: number | null = null) {
    super(message);
    this.name = 'RawgApiError';
    this.type = type;
    this.status = status;
  }
}

export function rawgErrorFromStatus(status: number): RawgApiError {
  switch (status) {
    case 401:
      return new RawgApiError('unauthorized', 'Invalid RAWG API key.', status);
    case 404:
      return new RawgApiError('not-found', 'The requested resource was not found on RAWG.', status);
    case 429:
      return new RawgApiError('rate-limited', 'RAWG rate limit reached.', status);
    default:
      return new RawgApiError('unknown', 'RAWG returned an unexpected response.', status);
  }
}

export function rawgErrorMessage(error: RawgApiError): string {
  switch (error.type) {
    case 'network':
      return 'No network connection. Check your internet and try again.';
    case 'unauthorized':
      return 'This API key is invalid. Get a key at https://rawg.io/keys.';
    case 'rate-limited':
      return 'RAWG rate limit reached. Wait a moment and try again.';
    case 'not-found':
      return 'Game not found on RAWG.';
    case 'timeout':
      return 'The request timed out. Try again.';
    case 'unknown':
      return 'Something went wrong while talking to RAWG. Try again.';
  }
}
