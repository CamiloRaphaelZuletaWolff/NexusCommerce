/**
 * The access token lives only in memory (never localStorage) — refresh is handled
 * by the HttpOnly `nexus_refresh` cookie. A tiny pub/sub lets the API client and
 * the auth provider share it without a circular import.
 */
let accessToken: string | null = null;
type Listener = (token: string | null) => void;
const listeners = new Set<Listener>();

export function getAccessToken() {
  return accessToken;
}

export function setAccessToken(token: string | null) {
  accessToken = token;
  for (const l of listeners) l(token);
}

export function subscribeToken(listener: Listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}
