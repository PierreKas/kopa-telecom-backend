'use client';
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '@/lib/api';
import { SESSION_KEY, UNAUTHORIZED_EVENT } from '@/lib/storage';
import type { AuthSession, User } from '@/lib/types';

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<AuthSession>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [loading, setLoading] = useState(true);

  // Restore a persisted session on first load - synchronous (localStorage), but read inside an
  // effect so server-rendered and first-client-render markup match (no session exists during
  // SSR).
  useEffect(() => {
    setSession(authApi.getSession());
    setLoading(false);
  }, []);

  // apiClient.ts fires this the moment any call discovers this session died server-side
  // (expired/revoked token) - dropping `session` here sends (app)/layout's own guard effect to
  // /login on its next render, with no manual reload or stale-page error needed.
  useEffect(() => {
    const onUnauthorized = () => setSession(null);
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const next = await authApi.login(username, password);
    window.localStorage.setItem(SESSION_KEY, JSON.stringify(next));
    setSession(next);
    return next;
  }, []);

  const logout = useCallback(() => {
    authApi.logout().catch(() => {});
    window.localStorage.removeItem(SESSION_KEY);
    setSession(null);
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user: session?.user ?? null,
    loading,
    login,
    logout,
  }), [session, loading, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider.');
  return ctx;
}
