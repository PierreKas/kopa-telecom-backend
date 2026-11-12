import { NextRequest, NextResponse } from 'next/server';
import { TOKEN_COOKIE } from '@/lib/server/authCookie';

const ADMIN_PORTAL_BASE_URL = process.env.ADMIN_PORTAL_API_BASE_URL ?? 'http://localhost:8085/kopa-admin-portal';

interface LoginEnvelope {
  success: boolean;
  data: { adminId: string; username: string; token: string; expiresAt: string } | null;
  error?: { code?: string; message?: string } | null;
}

export async function POST(req: NextRequest) {
  const body = await req.text();

  let upstream: Response;
  try {
    upstream = await fetch(`${ADMIN_PORTAL_BASE_URL}/api/v1/admin/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body,
      cache: 'no-store',
    });
  } catch {
    return NextResponse.json({ success: false, data: null, error: { message: 'Could not reach the server.' } }, { status: 502 });
  }

  let envelope: LoginEnvelope | null = null;
  try {
    envelope = await upstream.json();
  } catch {
    // no/invalid JSON body - envelope stays null, handled below
  }

  if (!upstream.ok || !envelope?.success || !envelope.data) {
    return NextResponse.json(
      envelope ?? { success: false, data: null, error: { message: `Unexpected error (${upstream.status}).` } },
      { status: upstream.status },
    );
  }

  const { adminId, username, token, expiresAt } = envelope.data;
  const response = NextResponse.json({ success: true, data: { adminId, username, expiresAt } });

  const maxAgeSeconds = Math.max(1, Math.floor((new Date(expiresAt).getTime() - Date.now()) / 1000));
  response.cookies.set(TOKEN_COOKIE, token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    path: '/',
    maxAge: maxAgeSeconds,
  });
  return response;
}
