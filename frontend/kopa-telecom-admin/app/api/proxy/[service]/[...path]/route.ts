import { NextRequest, NextResponse } from 'next/server';
import { TOKEN_COOKIE } from '@/lib/server/authCookie';

// Same-origin reverse proxy in front of kopa.admin-portal. The browser only ever talks to this
// Next.js server; the real service URL (and the bearer token) never reach client-side
// JavaScript. Only one backend service exists for this app today, but this stays keyed by a
// `service` segment (matching the pattern this was ported from) so a second admin-facing
// service could be added later without changing the client-side call shape.

const SERVICE_BASE_URLS: Record<string, string> = {
  admin: process.env.ADMIN_PORTAL_API_BASE_URL ?? 'http://localhost:8085/kopa-admin-portal',
};

async function forward(req: NextRequest, context: { params: Promise<{ service: string; path: string[] }> }) {
  const { service, path } = await context.params;
  const baseUrl = SERVICE_BASE_URLS[service];
  if (!baseUrl) {
    return NextResponse.json({ success: false, data: null, error: { message: 'Unknown service.' } }, { status: 404 });
  }

  const token = req.cookies.get(TOKEN_COOKIE)?.value;
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;

  const hasBody = req.method !== 'GET' && req.method !== 'HEAD';
  const url = `${baseUrl}/${path.join('/')}${req.nextUrl.search}`;

  let upstream: Response;
  try {
    upstream = await fetch(url, {
      method: req.method,
      headers,
      body: hasBody ? await req.text() : undefined,
      cache: 'no-store',
    });
  } catch {
    return NextResponse.json({ success: false, data: null, error: { message: 'Could not reach the server.' } }, { status: 502 });
  }

  const body = await upstream.text();
  const response = new NextResponse(body, {
    status: upstream.status,
    headers: { 'Content-Type': upstream.headers.get('Content-Type') ?? 'application/json' },
  });
  if (upstream.status === 401) {
    // This admin's own token is missing/expired/revoked - drop it so the browser doesn't keep
    // resending it.
    response.cookies.set(TOKEN_COOKIE, '', { maxAge: 0, path: '/' });
  }
  return response;
}

export { forward as GET, forward as POST, forward as PUT, forward as PATCH, forward as DELETE };
