// Name of the httpOnly cookie holding the bearer token issued by kopa.admin-portal's
// /api/v1/admin/auth/login. Server-only - read and written exclusively by the Route Handlers
// under app/api/auth/** and app/api/proxy/**, never by client-side code.
export const TOKEN_COOKIE = 'kopaadmin_token';
