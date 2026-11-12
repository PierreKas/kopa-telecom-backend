'use client';
import { useState } from 'react';
import { useAuth } from './providers/AuthProvider';

/**
 * Page topbar - a real, working search (submits to the Customers page, not a decorative
 * placeholder) and the logged-in admin's identity. No notification bell/mail icons: this app
 * has no notifications feature, and a fake badge count would be exactly the kind of fabricated
 * functionality worth avoiding.
 */
export default function Topbar({
  onOpenMobileNav,
  onSearchSubmit,
}: {
  onOpenMobileNav?: () => void;
  onSearchSubmit: (query: string) => void;
}) {
  const { user } = useAuth();
  const [query, setQuery] = useState('');

  return (
    <div
      style={{
        display: 'flex', alignItems: 'center', gap: 14, padding: '16px 28px',
        flexShrink: 0, flexWrap: 'wrap',
      }}
    >
      {onOpenMobileNav && (
        <button
          onClick={onOpenMobileNav}
          aria-label="Open menu"
          style={{ background: 'var(--kopa-ink)', border: 'none', color: 'white', width: 36, height: 36, borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', flexShrink: 0 }}
        >
          <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><line x1="3" y1="12" x2="21" y2="12" /><line x1="3" y1="6" x2="21" y2="6" /><line x1="3" y1="18" x2="21" y2="18" /></svg>
        </button>
      )}

      <form
        onSubmit={(e) => { e.preventDefault(); onSearchSubmit(query.trim()); }}
        style={{ position: 'relative', flex: 1, minWidth: 180, maxWidth: 420 }}
      >
        <svg style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)', color: 'var(--kopa-gray-400)' }} width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
          <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search customers by MSISDN or name…"
          style={{
            width: '100%', padding: '10px 14px 10px 38px', borderRadius: 10, border: '1px solid var(--kopa-gray-200)',
            background: 'white', fontSize: 13.5, outline: 'none', color: 'var(--kopa-gray-800)', fontFamily: 'inherit',
          }}
        />
      </form>

      <div style={{ flex: 1 }} />

      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <div style={{
          width: 36, height: 36, borderRadius: '50%', background: 'var(--kopa-blue)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, fontWeight: 700, color: 'white', flexShrink: 0,
        }}>
          {(user?.username ?? '?').charAt(0).toUpperCase()}
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.2 }}>
          <span style={{ fontSize: 13.5, fontWeight: 600, color: 'var(--kopa-navy)' }}>{user?.username ?? 'Admin'}</span>
          <span style={{ fontSize: 11.5, color: 'var(--kopa-gray-400)' }}>Administrator</span>
        </div>
      </div>
    </div>
  );
}
