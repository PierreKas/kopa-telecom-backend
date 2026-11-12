'use client';
import { useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import { useIsMobile } from '@/app/hooks/useIsMobile';

/** App shell: sidebar + topbar + main content area with the soft blob background used across
 *  the app. */
export default function AppShell({ children }: { children: React.ReactNode }) {
  const isMobile = useIsMobile();
  const pathname = usePathname();
  const router = useRouter();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const [lastPathname, setLastPathname] = useState(pathname);

  // Closes the mobile drawer on every page change (adjusted during render rather than an
  // effect).
  if (pathname !== lastPathname) {
    setLastPathname(pathname);
    setMobileNavOpen(false);
  }

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden', position: 'relative' }}>
      <Sidebar mobileOpen={mobileNavOpen} onCloseMobile={() => setMobileNavOpen(false)} />
      {isMobile && mobileNavOpen && (
        <div
          onClick={() => setMobileNavOpen(false)}
          style={{ position: 'fixed', inset: 0, background: 'rgba(72,26,56,0.5)', zIndex: 40 }}
        />
      )}
      <main style={{ flex: 1, overflowY: 'auto', overflowX: 'hidden', background: '#f5e9f1', display: 'flex', flexDirection: 'column', minWidth: 0, position: 'relative' }}>
        <div style={{ position: 'absolute', top: -120, left: -120, width: 480, height: 480, background: 'rgba(197,73,154,0.14)', borderRadius: '30% 70% 70% 30% / 30% 30% 70% 70%', pointerEvents: 'none', zIndex: 0 }} />
        <div style={{ position: 'absolute', top: 400, right: -140, width: 460, height: 460, background: 'rgba(72,26,56,0.06)', borderRadius: '70% 30% 30% 70% / 70% 70% 30% 30%', pointerEvents: 'none', zIndex: 0 }} />
        <div style={{ position: 'relative', zIndex: 1, flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
          <Topbar
            onOpenMobileNav={isMobile ? () => setMobileNavOpen(true) : undefined}
            onSearchSubmit={(q) => router.push(q ? `/customers?q=${encodeURIComponent(q)}` : '/customers')}
          />
          {children}
        </div>
      </main>
    </div>
  );
}
