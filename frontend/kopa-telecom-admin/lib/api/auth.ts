import * as authClient from '../http/authClient';
import type { AuthSession } from '../types';
import { SESSION_KEY } from '../storage';

export async function login(username: string, password: string): Promise<AuthSession> {
  return authClient.login(username, password);
}

export async function logout(): Promise<void> {
  await authClient.logout();
}

/** Sync on purpose - read once on mount to decide the initial auth state without a loading flash. */
export function getSession(): AuthSession | null {
  if (typeof window === 'undefined') return null;
  try {
    const raw = window.localStorage.getItem(SESSION_KEY);
    return raw ? (JSON.parse(raw) as AuthSession) : null;
  } catch {
    return null;
  }
}
