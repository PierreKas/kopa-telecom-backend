// Backed by the real kopa.admin-portal service (see lib/http/*) - the only thing kept in
// localStorage is the session's own optimistic-UI mirror, on first paint before the httpOnly
// cookie has been verified. Real auth is always the httpOnly cookie the backend sets (see
// app/api/auth/*) - this mirror never holds a password or token.
export const SESSION_KEY = 'kopaadmin:session';

// Fired on `window` whenever apiClient.ts sees a 401 from a session-backed call - AuthProvider
// listens to clear its state (which sends the (app)/layout guard to /login automatically) and
// ToastProvider listens to swallow the error toast that call's own catch block is about to
// raise, so an expired session reads as a silent redirect to the login form instead of an
// "unauthorized" error on the stale page.
export const UNAUTHORIZED_EVENT = 'kopaadmin:unauthorized';
