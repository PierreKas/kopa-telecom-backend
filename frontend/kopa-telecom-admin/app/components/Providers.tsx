'use client';
import { ToastProvider } from './providers/ToastProvider';
import { AuthProvider } from './providers/AuthProvider';

export default function Providers({ children }: { children: React.ReactNode }) {
  return (
    <ToastProvider>
      <AuthProvider>{children}</AuthProvider>
    </ToastProvider>
  );
}
