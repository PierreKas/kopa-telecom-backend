'use client';
import { useState } from 'react';
import { usePathname } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { useAuth } from './providers/AuthProvider';
import { useIsMobile } from '@/app/hooks/useIsMobile';

type NavItem = { label: string; href: string; icon: React.ReactNode };

const icon = (path: React.ReactNode) => (
  <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">{path}</svg>
);

const NAV: NavItem[] = [
  { label: 'Dashboard', href: '/dashboard', icon: icon(<><rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" /><rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" /></>) },
  { label: 'Customers', href: '/customers', icon: icon(<><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></>) },
  { label: 'Transactions', href: '/transactions', icon: icon(<><line x1="12" y1="1" x2="12" y2="23" /><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" /></>) },
  { label: 'SMS Messages', href: '/sms-messages', icon: icon(<><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" /></>) },
];

const ChevronLeft = () => (
  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6" /></svg>
);
const ChevronRight = () => (
  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6" /></svg>
);
const CloseIcon = () => (
  <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
);

export default function Sidebar({ mobileOpen = false, onCloseMobile }: { mobileOpen?: boolean; onCloseMobile?: () => void }) {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const isMobile = useIsMobile();

  // On mobile the sidebar is a full-screen overlay drawer (no collapsed mode) - on desktop it
  // keeps its collapsible behavior.
  const effectiveCollapsed = isMobile ? false : collapsed;
  const W = effectiveCollapsed ? 68 : 240;

  const closeOnMobileNav = () => {
    if (isMobile) onCloseMobile?.();
  };

  return (
    <aside style={{
      width: W, minWidth: W, background: 'var(--kopa-ink)', display: 'flex', flexDirection: 'column',
      flexShrink: 0, height: '100vh', overflowY: 'auto',
      transition: isMobile ? 'transform 0.25s ease' : 'width 0.22s ease, min-width 0.22s ease',
      position: isMobile ? 'fixed' : 'relative', top: 0, left: 0, zIndex: 50,
      transform: isMobile ? `translateX(${mobileOpen ? '0' : '-100%'})` : 'none',
    }}>
      {/* Logo */}
      <div style={{
        padding: effectiveCollapsed ? '20px 0' : '22px 22px 18px', borderBottom: '1px solid rgba(255,255,255,0.08)',
        display: 'flex', alignItems: 'center', justifyContent: effectiveCollapsed ? 'center' : 'space-between', transition: 'padding 0.22s ease',
      }}>
        {effectiveCollapsed ? (
          <Image src="/images/logo-icon.png" alt="Kopa Telecom" width={32} height={32} style={{ objectFit: 'contain', flexShrink: 0 }} />
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, minWidth: 0 }}>
            <Image src="/images/logo-icon.png" alt="" width={32} height={32} style={{ objectFit: 'contain', flexShrink: 0 }} />
            <div style={{ minWidth: 0, overflow: 'hidden' }}>
              <div style={{ color: 'white', fontWeight: 700, fontSize: 15, letterSpacing: '-0.3px', whiteSpace: 'nowrap' }}>Kopa Telecom</div>
              <div style={{ color: 'rgba(255,255,255,0.4)', fontSize: 11, marginTop: 1 }}>Admin Portal</div>
            </div>
          </div>
        )}
        {isMobile && (
          <button
            onClick={onCloseMobile}
            aria-label="Close menu"
            style={{ background: 'rgba(255,255,255,0.1)', border: 'none', color: 'white', width: 32, height: 32, borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', flexShrink: 0 }}
          >
            <CloseIcon />
          </button>
        )}
      </div>

      {!isMobile && (
        <button
          onClick={() => setCollapsed(!collapsed)}
          title={collapsed ? 'Expand' : 'Collapse'}
          style={{
            position: 'absolute', top: 64, right: 8, width: 28, height: 28, borderRadius: 8,
            background: 'rgba(255,255,255,0.08)', border: '1px solid rgba(255,255,255,0.15)', color: 'rgba(255,255,255,0.7)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', zIndex: 10, padding: 0,
            transition: 'background 0.15s ease, color 0.15s ease',
          }}
          onMouseEnter={(e) => { e.currentTarget.style.background = 'var(--kopa-blue)'; e.currentTarget.style.color = 'white'; }}
          onMouseLeave={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.08)'; e.currentTarget.style.color = 'rgba(255,255,255,0.7)'; }}
        >
          {collapsed ? <ChevronRight /> : <ChevronLeft />}
        </button>
      )}

      {/* Nav */}
      <nav style={{ flex: 1, padding: effectiveCollapsed ? '16px 8px' : '16px 12px', transition: 'padding 0.22s ease' }}>
        {!effectiveCollapsed && (
          <div style={{ fontSize: 10, fontWeight: 600, color: 'rgba(255,255,255,0.3)', letterSpacing: '0.08em', textTransform: 'uppercase', paddingLeft: 12, marginBottom: 8 }}>
            Menu
          </div>
        )}
        {NAV.map((item) => {
          const active = pathname === item.href;
          return (
            <Link key={item.href} href={item.href} style={{ textDecoration: 'none' }} title={effectiveCollapsed ? item.label : undefined} onClick={closeOnMobileNav}>
              <div style={{
                display: 'flex', alignItems: 'center', justifyContent: effectiveCollapsed ? 'center' : 'flex-start',
                gap: effectiveCollapsed ? 0 : 10, padding: effectiveCollapsed ? '10px 0' : '10px 12px', borderRadius: 8, marginBottom: 2,
                cursor: 'pointer', background: active ? 'rgba(197,73,154,0.18)' : 'transparent',
                color: active ? 'var(--kopa-blue-light)' : 'rgba(255,255,255,0.6)', fontWeight: active ? 600 : 400, fontSize: 14,
                transition: 'all 0.15s ease', borderLeft: active ? '3px solid var(--kopa-blue-light)' : '3px solid transparent',
                overflow: 'hidden', whiteSpace: 'nowrap',
              }}>
                <span style={{ opacity: active ? 1 : 0.7, display: 'flex', flexShrink: 0 }}>{item.icon}</span>
                {!effectiveCollapsed && <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>{item.label}</span>}
              </div>
            </Link>
          );
        })}
      </nav>

      {/* User + logout */}
      <div style={{ padding: effectiveCollapsed ? '14px 8px' : '14px 16px', borderTop: '1px solid rgba(255,255,255,0.08)', transition: 'padding 0.22s ease' }}>
        {!effectiveCollapsed && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
            <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--kopa-blue)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 700, color: 'white', flexShrink: 0 }}>
              {(user?.username ?? '?').charAt(0).toUpperCase()}
            </div>
            <div style={{ minWidth: 0 }}>
              <div style={{ color: 'white', fontSize: 13, fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user?.username}</div>
              <div style={{ color: 'rgba(255,255,255,0.4)', fontSize: 11 }}>Administrator</div>
            </div>
          </div>
        )}
        {effectiveCollapsed && (
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 10 }} title={user?.username}>
            <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--kopa-blue)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 700, color: 'white' }}>
              {(user?.username ?? '?').charAt(0).toUpperCase()}
            </div>
          </div>
        )}
        <button
          onClick={logout}
          title={effectiveCollapsed ? 'Log out' : undefined}
          style={{
            width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: effectiveCollapsed ? 0 : 8,
            padding: '8px', borderRadius: 8, border: '1px solid rgba(255,255,255,0.15)', background: 'transparent',
            color: 'rgba(255,255,255,0.7)', fontSize: 13, fontWeight: 500, cursor: 'pointer',
          }}
        >
          <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          {!effectiveCollapsed && 'Log out'}
        </button>
      </div>
    </aside>
  );
}
