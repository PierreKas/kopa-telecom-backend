import { NextRequest, NextResponse } from 'next/server';
import { TOKEN_COOKIE } from '@/lib/server/authCookie';

const ADMIN_PORTAL_BASE_URL = process.env.ADMIN_PORTAL_API_BASE_URL ?? 'http://localhost:8085/kopa-admin-portal';

export async function POST(req: NextRequest) {
  const token = req.cookies.get(TOKEN_COOKIE)?.value;
  if (token) {
    try {
      await fetch(`${ADMIN_PORTAL_BASE_URL}/api/v1/admin/auth/logout`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
        cache: 'no-store',
      });
    } catch {
      // ignore - we clear the cookie either way
    }
  }
  const response = NextResponse.json({ success: true, data: null });
  response.cookies.set(TOKEN_COOKIE, '', { maxAge: 0, path: '/' });
  return response;
}
