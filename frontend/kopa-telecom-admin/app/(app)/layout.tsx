'use client';
import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/app/components/providers/AuthProvider';
import AppShell from '@/app/components/AppShell';
import { FullPageLoader } from '@/app/components/ui';

// Access guard: not logged in -> /login, otherwise render the rest of the app behind AppShell.
export default function AppLayout({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;
    if (!user) {
      router.replace('/login');
    }
  }, [loading, user, router]);

  if (loading || !user) return <FullPageLoader />;

  return <AppShell>{children}</AppShell>;
}
