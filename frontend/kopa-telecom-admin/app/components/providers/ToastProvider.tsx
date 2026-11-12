'use client';
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { UNAUTHORIZED_EVENT } from '@/lib/storage';

type ToastType = 'success' | 'error' | 'info';
interface Toast {
  id: number;
  type: ToastType;
  message: string;
}

interface ToastContextValue {
  success: (message: string) => void;
  error: (message: string) => void;
  info: (message: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

const STYLE: Record<ToastType, { border: string; icon: string }> = {
  success: { border: 'var(--kopa-green)', icon: '✓' },
  error: { border: 'var(--kopa-red)', icon: '!' },
  info: { border: 'var(--kopa-gray-400)', icon: 'ℹ' },
};

const DURATION_MS = 3800;
let counter = 0;

// How long after a forced logout (see UNAUTHORIZED_EVENT) an "error" toast gets swallowed
// instead of shown - long enough to cover the failing call's own catch block firing
// toast.error(...) right after the event, so an expired session reads as a silent redirect to
// the login form instead of a stale-page "unauthorized" error.
const SUPPRESS_ERRORS_MS = 3000;

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const suppressErrorsUntilRef = useRef(0);

  useEffect(() => {
    const onUnauthorized = () => {
      suppressErrorsUntilRef.current = Date.now() + SUPPRESS_ERRORS_MS;
      setToasts((prev) => prev.filter((t) => t.type !== 'error'));
    };
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
  }, []);

  const dismiss = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback((type: ToastType, message: string) => {
    if (type === 'error' && Date.now() < suppressErrorsUntilRef.current) return;
    const id = ++counter;
    setToasts((prev) => [...prev, { id, type, message }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), DURATION_MS);
  }, []);

  // Stable functions + a memoized context value - an unmemoized value here would turn a single
  // failed request into an unbounded retry loop via a data-loading effect downstream.
  const success = useCallback((m: string) => push('success', m), [push]);
  const error = useCallback((m: string) => push('error', m), [push]);
  const info = useCallback((m: string) => push('info', m), [push]);
  const value = useMemo<ToastContextValue>(() => ({ success, error, info }), [success, error, info]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div style={{ position: 'fixed', top: 20, right: 20, display: 'flex', flexDirection: 'column', gap: 12, zIndex: 2000, maxWidth: 380, width: '100%' }}>
        {toasts.map((t) => {
          const s = STYLE[t.type];
          return (
            <div
              key={t.id}
              style={{
                background: 'white', borderRadius: 12, borderLeft: `4px solid ${s.border}`, padding: '14px 16px',
                display: 'flex', alignItems: 'flex-start', gap: 12, fontSize: 14, fontWeight: 500,
                boxShadow: '0 12px 32px rgba(72,26,56,0.18), 0 2px 8px rgba(72,26,56,0.08)',
                animation: 'toast-in 0.25s cubic-bezier(0.16,1,0.3,1)', overflow: 'hidden', position: 'relative',
              }}
            >
              <span style={{ width: 24, height: 24, borderRadius: '50%', background: s.border, color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 700, flexShrink: 0, marginTop: 1 }}>
                {s.icon}
              </span>
              <span style={{ color: 'var(--kopa-gray-800)', lineHeight: 1.5, flex: 1, paddingTop: 2 }}>{t.message}</span>
              <button
                onClick={() => dismiss(t.id)}
                aria-label="Close"
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--kopa-gray-400)', padding: 2, flexShrink: 0, display: 'flex' }}
              >
                <svg width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
              </button>
              <div
                style={{
                  position: 'absolute', bottom: 0, left: 0, right: 0, height: 3, background: s.border,
                  transformOrigin: 'left', animation: `toast-shrink ${DURATION_MS}ms linear forwards`,
                }}
              />
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used inside ToastProvider.');
  return ctx;
}
