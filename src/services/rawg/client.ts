import { RawgApiError, rawgErrorFromStatus } from '@/services/rawg/errors';
import type {
  RawgGame,
  RawgGamesResponse,
  RawgNamedEntity,
  RawgPlatform,
} from '@/services/rawg/types';

const RAWG_BASE_URL = 'https://api.rawg.io/api';
const REQUEST_TIMEOUT_MS = 10000;

function isRawgNamedEntity(value: unknown): value is RawgNamedEntity {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  return typeof (value as Record<string, unknown>).name === 'string';
}

function isRawgPlatform(value: unknown): value is RawgPlatform {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const platform = (value as Record<string, unknown>).platform;
  return isRawgNamedEntity(platform);
}

function isRawgGame(value: unknown): value is RawgGame {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return (
    typeof candidate.id === 'number' &&
    typeof candidate.name === 'string' &&
    Array.isArray(candidate.genres) &&
    candidate.genres.every(isRawgNamedEntity) &&
    Array.isArray(candidate.platforms) &&
    candidate.platforms.every(isRawgPlatform) &&
    Array.isArray(candidate.tags) &&
    candidate.tags.every(isRawgNamedEntity)
  );
}

function isRawgGamesResponse(value: unknown): value is RawgGamesResponse {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return (
    typeof candidate.count === 'number' &&
    Array.isArray(candidate.results) &&
    candidate.results.every(isRawgGame)
  );
}

async function request<T>(
  path: string,
  apiKey: string,
  guard: (value: unknown) => value is T,
): Promise<T> {
  const url = `${RAWG_BASE_URL}${path}${path.includes('?') ? '&' : '?'}key=${apiKey}`;
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    const response = await fetch(url, { signal: controller.signal });
    if (!response.ok) {
      throw rawgErrorFromStatus(response.status);
    }
    const json: unknown = await response.json();
    if (!guard(json)) {
      throw new RawgApiError('unknown', 'Unexpected RAWG response shape.');
    }
    return json;
  } catch (error) {
    if (error instanceof RawgApiError) {
      throw error;
    }
    if (error instanceof Error && error.name === 'AbortError') {
      throw new RawgApiError('timeout', 'The request timed out.');
    }
    throw new RawgApiError('network', 'Network request failed.');
  } finally {
    clearTimeout(timeoutId);
  }
}

export async function searchGames(query: string, apiKey: string): Promise<RawgGame[]> {
  const path = `/games?search=${encodeURIComponent(query)}&page_size=20`;
  const response = await request<RawgGamesResponse>(path, apiKey, isRawgGamesResponse);
  return response.results;
}

export async function fetchGame(id: number, apiKey: string): Promise<RawgGame> {
  const response = await request<RawgGame>(`/games/${id}`, apiKey, isRawgGame);
  return response;
}

export async function checkApiKey(apiKey: string): Promise<RawgApiError | null> {
  try {
    await request<RawgGamesResponse>('/games?page_size=1', apiKey, isRawgGamesResponse);
    return null;
  } catch (error) {
    if (error instanceof RawgApiError) {
      return error;
    }
    return new RawgApiError('unknown', 'API key check failed.');
  }
}
