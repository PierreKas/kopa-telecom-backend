import { SESSION_KEY, UNAUTHORIZED_EVENT } from '../storage';

// Generic client for kopa.admin-portal. Its envelope shape is {success, data, error, path,
// timestamp} on every endpoint - unlike a {success, message, data} shape, the error detail
// lives under `error.message`, not a top-level `message`. Auth is handled by an httpOnly cookie
// the browser attaches automatically to same-origin requests (see app/api/proxy/**) - this
// client never sees or stores the real bearer token.

export interface ApiEnvelope<T> {
  success: boolean;
  data: T | null;
  error?: { code?: string; message?: string } | null;
}

export class ApiError extends Error {
  status: number;
  code?: string;
  constructor(message: string, status: number, code?: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

/** Thin fetch wrapper: unwraps the ApiResponse envelope, throws ApiError on failure. */
export async function apiRequest<T>(baseUrl: string, path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init.headers as Record<string, string> | undefined),
  };

  let res: Response;
  try {
    res = await fetch(`${baseUrl}${path}`, { ...init, headers });
  } catch {
    throw new ApiError('Could not reach the server. Check your connection.', 0);
  }

  let envelope: ApiEnvelope<T> | null = null;
  try {
    envelope = await res.json();
  } catch {
    // no/invalid JSON body (e.g. a 204 No Content) - envelope stays null, handled below
  }

  // `''` is /api/auth/login|logout (a bad password is a normal 401 to show on the login form
  // itself, not a "your session expired" event) - only a real proxied /api/proxy/admin/** call
  // reaching here on a 401 means this admin's own session died server-side.
  const isSessionBackedCall = baseUrl.startsWith('/api/proxy/');

  if (res.status === 401 && typeof window !== 'undefined' && isSessionBackedCall) {
    window.localStorage.removeItem(SESSION_KEY);
    window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
  }

  if (res.status === 204) return null as T;

  if (!res.ok || !envelope || !envelope.success) {
    throw new ApiError(envelope?.error?.message ?? `Unexpected error (${res.status}).`, res.status, envelope?.error?.code);
  }
  return envelope.data as T;
}
