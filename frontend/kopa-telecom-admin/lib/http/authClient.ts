import { apiRequest, ApiError } from './apiClient';
import type { AuthSession } from '../types';

// What OUR /api/auth/login route returns - it strips the real bearer token out before
// responding (the token goes straight into an httpOnly cookie instead, see that route).
interface LoginRouteData {
  adminId: string;
  username: string;
  expiresAt: string;
}

export async function login(username: string, password: string): Promise<AuthSession> {
  const res = await apiRequest<LoginRouteData>('', '/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  return { user: { id: res.adminId, username: res.username }, expiresAt: res.expiresAt };
}

export function logout(): Promise<null> {
  return apiRequest<null>('', '/api/auth/logout', { method: 'POST' });
}

export { ApiError };
