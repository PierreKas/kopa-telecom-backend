'use client';
import { useEffect, useState } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/app/components/providers/AuthProvider';

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  // Computed on the client, not during prerender - new Date() is flagged as an impure read if
  // evaluated inline during a static prerender of this Client Component.
  const [year, setYear] = useState<number | null>(null);
  useEffect(() => setYear(new Date().getFullYear()), []);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await login(username, password);
      router.replace('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not log in.');
      setBusy(false);
    }
  };

  return (
    <>
      <style>{`
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        .lr { min-height: 100vh; width: 100%; display: flex; align-items: center; justify-content: center;
          background: #f5e9f1; padding: 32px 16px;
          font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
          position: relative; overflow: hidden; }
        .lr-bg-shape1 { position: fixed; top: -120px; left: -120px; width: 480px; height: 480px;
          background: rgba(197,73,154,0.2); border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
          pointer-events: none; z-index: 0; }
        .lr-bg-shape2 { position: fixed; bottom: -160px; right: -100px; width: 520px; height: 520px;
          background: rgba(72,26,56,0.1); border-radius: 70% 30% 30% 70% / 70% 70% 30% 30%;
          pointer-events: none; z-index: 0; }
        .lr-bg-shape3 { position: fixed; top: 40%; left: 55%; width: 300px; height: 300px;
          background: rgba(209,249,49,0.14); border-radius: 50%; pointer-events: none; z-index: 0; }
        .lr-card { display: flex; width: 100%; max-width: 920px; min-height: 560px; border-radius: 20px;
          overflow: hidden; box-shadow: 0 24px 80px rgba(72,26,56,0.18), 0 4px 20px rgba(0,0,0,0.08);
          position: relative; z-index: 1; animation: fade-in-up 0.5s cubic-bezier(0.16,1,0.3,1) both; }
        .lr-form-panel { flex: 1; background: #fff; padding: 52px 50px 44px;
          display: flex; flex-direction: column; justify-content: center; min-width: 0; }
        .lr-welcome { font-size: 27px; font-weight: 800; color: var(--kopa-ink); letter-spacing: -0.5px; margin-top: 24px; }
        .lr-welcome-sub { font-size: 14px; color: #8b7187; margin-top: 6px; margin-bottom: 26px; line-height: 1.5; }
        .lr-field { margin-bottom: 16px; }
        .lr-label { display: block; font-size: 13px; font-weight: 600; color: #3a2434; margin-bottom: 7px; }
        .lr-input { width: 100%; padding: 12px 16px; border: 1.5px solid #ededed; border-radius: 8px;
          font-size: 14px; color: var(--kopa-ink); background: #f8f9fa; outline: none;
          transition: border-color 0.15s, box-shadow 0.15s, background 0.15s; font-family: inherit; }
        .lr-input:focus { border-color: var(--kopa-blue); background: #fff; box-shadow: 0 0 0 3px rgba(197,73,154,0.18); }
        .lr-input::placeholder { color: #b3a2ae; }
        .lr-btn { width: 100%; padding: 13px; margin-top: 6px; background: var(--kopa-blue); color: white; border: none;
          border-radius: 8px; font-size: 14px; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase;
          cursor: pointer; transition: background 0.15s, transform 0.15s, box-shadow 0.15s;
          box-shadow: 0 3px 14px rgba(197,73,154,0.35); font-family: inherit; }
        .lr-btn:hover:not(:disabled) { background: #a83a82; transform: translateY(-1px); box-shadow: 0 6px 18px rgba(197,73,154,0.42); }
        .lr-btn:disabled { opacity: 0.6; cursor: not-allowed; }
        .lr-error { background: #fff5f5; border: 1.5px solid #fc8181; color: #c53030;
          border-radius: 8px; padding: 10px 14px; font-size: 13px; margin-bottom: 14px; }
        .lr-pw-wrap { position: relative; }
        .lr-pw-toggle { position: absolute; right: 4px; top: 50%; transform: translateY(-50%);
          width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
          background: none; border: none; cursor: pointer; color: #a68a9f; padding: 0; }
        .lr-brand-panel { flex: 1; background: linear-gradient(160deg, var(--kopa-ink) 0%, var(--kopa-navy) 55%, var(--kopa-blue) 130%);
          display: flex; flex-direction: column; align-items: center; justify-content: center;
          text-align: center; padding: 52px 44px; position: relative; overflow: hidden; min-width: 0; }
        .lr-brand-panel::before { content: ''; position: absolute; inset: 0;
          background-image: radial-gradient(circle, rgba(209,249,49,0.18) 1px, transparent 1px);
          background-size: 28px 28px; pointer-events: none; }
        .lr-brand-panel::after { content: ''; position: absolute; top: 50%; left: 50%;
          transform: translate(-50%, -50%); width: 420px; height: 420px; border-radius: 50%;
          border: 1px solid rgba(209,249,49,0.14); pointer-events: none; }
        .lr-brand-inner { position: relative; z-index: 1; display: flex; flex-direction: column; align-items: center; }
        .lr-brand-icon { width: 200px; height: auto; margin-bottom: 30px; }
        .lr-brand-title { font-size: 30px; font-weight: 800; color: #fff; line-height: 1.25; letter-spacing: -0.6px; margin-bottom: 18px; }
        .lr-brand-title span { color: var(--kopa-secondary); }
        .lr-brand-divider { width: 48px; height: 3px; background: var(--kopa-secondary); border-radius: 2px; margin: 0 auto 20px; }
        .lr-brand-desc { font-size: 14.5px; color: rgba(255,255,255,0.65); line-height: 1.75; max-width: 280px; }
        .lr-brand-foot { position: absolute; bottom: 24px; left: 0; right: 0; text-align: center;
          font-size: 12px; color: rgba(255,255,255,0.3); z-index: 1; }
        @media (max-width: 740px) {
          .lr-card { flex-direction: column; min-height: auto; border-radius: 16px; }
          .lr-brand-panel { order: -1; padding: 40px 28px 32px; }
          .lr-brand-icon { width: 130px; margin-bottom: 16px; }
          .lr-brand-title { font-size: 23px; } .lr-brand-foot { position: static; margin-top: 20px; }
          .lr-form-panel { padding: 36px 28px 40px; }
        }
        @media (max-width: 400px) {
          .lr-form-panel { padding: 28px 20px 32px; } .lr-brand-panel { padding: 32px 20px 28px; }
        }
      `}</style>

      <div className="lr">
        <div className="lr-bg-shape1" /><div className="lr-bg-shape2" /><div className="lr-bg-shape3" />
        <div className="lr-card">
          {/* Left: form */}
          <div className="lr-form-panel">
            <Image src="/images/logo-wordmark.png" alt="Kopa Telecom" width={350} height={180} style={{ height: 48, width: 'auto', objectFit: 'contain', display: 'block' }} priority />
            <div className="lr-welcome">Welcome,</div>
            <div className="lr-welcome-sub">Sign in to access the Kopa Telecom admin portal.</div>
            <form onSubmit={submit}>
              <div className="lr-field">
                <label className="lr-label" htmlFor="un">Username</label>
                <input id="un" className="lr-input" value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter your username" autoComplete="username" autoFocus />
              </div>
              <div className="lr-field">
                <label className="lr-label" htmlFor="pw">Password</label>
                <div className="lr-pw-wrap">
                  <input id="pw" className="lr-input" type={showPw ? 'text' : 'password'} value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter your password" autoComplete="current-password" style={{ paddingRight: 40 }} />
                  <button type="button" className="lr-pw-toggle" onClick={() => setShowPw((v) => !v)} tabIndex={-1}
                    aria-label={showPw ? 'Hide password' : 'Show password'}>
                    {showPw ? (
                      <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" /><line x1="1" y1="1" x2="23" y2="23" /></svg>
                    ) : (
                      <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
                    )}
                  </button>
                </div>
              </div>
              {error && <div className="lr-error">{error}</div>}
              <button type="submit" className="lr-btn" disabled={busy}>
                {busy ? 'Signing in…' : 'Sign in'}
              </button>
            </form>
          </div>

          {/* Right: brand panel */}
          <div className="lr-brand-panel">
            <div className="lr-brand-inner">
              <Image src="/images/logo-wordmark.png" alt="" width={350} height={180} className="lr-brand-icon" aria-hidden="true"
                style={{ filter: 'brightness(0) invert(1)', opacity: 0.95 }} />
              <h2 className="lr-brand-title">All your administration,<br />in <span>one place</span>.</h2>
              <div className="lr-brand-divider" />
            </div>
            <div className="lr-brand-foot">© {year ?? ''} Kopa Telecom</div>
          </div>
        </div>
      </div>
    </>
  );
}
