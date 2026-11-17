# Kopa Telecom — Backend

Spring Boot backend for the Kopa Telecom mobile app and the NextBridge Tech admin web portal.
Six independently deployable microservices, one shared Postgres instance, one shared RabbitMQ
exchange, no API gateway, no shared JAR between services.

For the original design rationale (why read-only mirrors instead of REST, why async-by-default,
naming conventions, etc.) see [`architecture-reference.md`](./architecture-reference.md) and the
schema reference at [`../db-diagram.md`](../db-diagram.md). This README documents the **current,
deployed state** of the system and — in depth — how it's built and run with Docker.

## Table of contents

1. [Architecture at a glance](#1-architecture-at-a-glance)
2. [The services](#2-the-services)
3. [Cross-service communication](#3-cross-service-communication)
4. [Data model](#4-data-model)
5. [Configuration & secrets](#5-configuration--secrets)
6. [Docker deployment](#6-docker-deployment)
7. [Exposing the backend beyond the local network](#7-exposing-the-backend-beyond-the-local-network)
8. [Troubleshooting](#8-troubleshooting)
9. [Known limitations / open items](#9-known-limitations--open-items)

---

## 1. Architecture at a glance

```
                         ┌───────────────────────────┐
                         │   Mobile app (iOS/Android) │
                         └──────────────┬─────────────┘
                                        │ JSON, /api/v1/**
              ┌─────────────────────────┼─────────────────────────┐
              ▼                         ▼                         ▼
     kopa.customer-identity   kopa.customer-self-care     kopa.notifications
       (8083)                   (8081)                      (8084)
              │                         │                         │
              └───────────┬─────────────┘         sync OTP send   │
                          │ read-only mirror                      │
                          │ (customers, auth_tokens)               │
                          ▼                                       ▼
                 kopa.middleware-ktrn (8082)              kopa.middleware-sms (8086)
                          │                                       │
                          ▼                                       ▼
                  KTRN / RBSS-CCBS BSS                    InTouch SMS Gateway
                  (VPN-only, www.4glte.rw)                (public internet)

     ┌───────────────────────────┐
     │  Admin web portal (Next.js) │
     └──────────────┬─────────────┘
                    │ JSON, /api/v1/admin/**
                    ▼
           kopa.admin-portal (8085)  ── read-only mirrors ──▶  every schema above
```

- Every service is a standalone Spring Boot app with its own `Dockerfile`, its own Postgres
  schema, and its own `pom.xml` — nothing is shared at build time.
- **Mobile** (`kopa.customer-identity`, `kopa.customer-self-care`, `kopa.notifications`) and
  **Web/Admin** (`kopa.admin-portal`) are fully separate credential spaces. A mobile bearer token
  can never authenticate against the admin portal and vice versa.
- **Middleware** (`kopa.middleware-ktrn`, `kopa.middleware-sms`) is stateless shared
  infrastructure underneath both sides — it has no Postgres schema of its own, it just brokers
  the 3rd-party protocols (KTRN/RBSS-CCBS, InTouch SMS).

---

## 2. The services

| Service | Port | Context path | Schema | Purpose |
|---|---|---|---|---|
| `kopa.customer-identity` | 8083 | `/kopa-customer-identity` | `customer_identity` | Registration/activation (KTRN-backed KYC), login, PIN management, sessions, profile |
| `kopa.customer-self-care` | 8081 | `/kopa-customer-self-care` | `customer_self_care` | Balance, usage, bundles, recharge, statement/transaction history, transaction limits |
| `kopa.notifications` | 8084 | `/kopa-notifications` | `notifications` | SMS orchestration (OTP + transactional SMS), delivery tracking, send-log |
| `kopa.admin-portal` | 8085 | `/kopa-admin-portal` | `admin_portal` | Admin accounts/RBAC, customer/transaction/SMS lookups, dashboards — read-only across every other schema |
| `kopa.middleware-ktrn` | 8082 | `/kopa-middleware-ktrn` | *(stateless)* | Every KTRN/RBSS-CCBS operation (balance, bundles, recharge, registration, activation, identity verification, MSISDN lookup) |
| `kopa.middleware-sms` | 8086 | `/kopa-middleware-sms` | *(stateless)* | InTouch SMS send + delivery-receipt webhook |

Folder layout mirrors this split:

```
backend/
├── kopa-config.yml          # local-dev shared config (not committed)
├── .env                     # Docker deployment secrets (not committed)
├── docker-compose.yml
├── microservices/
│   ├── mobile/
│   │   ├── kopa.customer-identity/
│   │   ├── kopa.customer-self-care/
│   │   └── kopa.notifications/
│   └── web/
│       └── kopa.admin-portal/
├── middleware/
│   ├── kopa.middleware-ktrn/
│   └── kopa.middleware-sms/
└── tools/                   # dev-only helpers (e.g. mock KTRN server), not a deployed service
```

See `architecture-reference.md` §5–§7 for the full endpoint-by-endpoint breakdown of each
service; the table above is the quick-reference version.

---

## 3. Cross-service communication

Three mechanisms, used deliberately for different reasons:

1. **Async (default): RabbitMQ.** One topic exchange, `kopa.events`. `customer-identity` and
   `customer-self-care` publish events (`customer.activated`, `transaction.completed`,
   `transaction.failed`); `kopa.notifications` consumes them to send confirmation/failure SMS.
   Nothing blocks the caller's HTTP response for these.
2. **Sync internal REST (the exception): used only when the caller genuinely cannot proceed
   without an answer** — e.g. `customer-self-care → kopa.notifications` to dispatch an OTP before
   telling the client "code sent," or any Microservice → Middleware call (KTRN/InTouch operations
   are inherently synchronous). Every `/internal/v1/**` path requires a shared
   `X-Internal-Api-Key` header instead of a bearer token — one key per target service
   (`KOPA_INTERNAL_<SERVICE>_API_KEY`), not one shared secret for everything.
3. **Read-only mirrors (the other exception):** every Mobile service mirrors
   `customer_identity.customers` / `.auth_tokens` locally (read-only JPA entities against the same
   Postgres schema) to validate bearer tokens without a network call per request.
   `kopa.admin-portal` does the same thing against every other schema for its dashboards/reports —
   it never writes to a Mobile-side table. **Important:** because these are independent entity
   copies, any enum value or column added to the source-of-truth service's table (e.g. a new
   `TransactionStatus`, or a new `otp_expires_at` column) must be re-added to `admin-portal`'s
   mirror entity by hand — nothing keeps the two in sync automatically.

---

## 4. Data model

One physical Postgres database (`kopa_db`), one schema per service (`customer_identity`,
`customer_self_care`, `notifications`, `admin_portal`). Each service owns its schema via Flyway,
with a **distinct Flyway history table name per service** (`flyway_schema_history_<service>`) so
their migration bookkeeping doesn't collide inside the shared database. Middleware services have
no schema at all — they're stateless protocol bridges.

Full table-level schema: [`../db-diagram.md`](../db-diagram.md).

---

## 5. Configuration & secrets

Two parallel configuration paths exist, serving different environments:

- **Local development** — `kopa-config.yml` at `backend/` root (not committed), imported via
  `spring.config.import: optional:file:/absolute/path/kopa-config.yml` in each service's
  `application.yaml`. Real DB/KTRN/InTouch values live here when running services directly on a
  dev machine.
- **Docker deployment** — `.env` at `backend/` root (not committed), loaded into every service
  container via `env_file: .env` in `docker-compose.yml`. The `optional:file:...kopa-config.yml`
  import above silently no-ops inside a container (the absolute dev-machine path doesn't exist
  there) — instead, **Spring Boot's relaxed environment-variable binding** satisfies the exact
  same property paths directly from the container's environment. For example `KOPA_DB_URL` as an
  env var automatically binds to the `kopa.db.url` property — no profile switch, no
  `application-docker.yaml` needed. (A few early `application-docker.yaml` files exist in three
  services from an earlier, abandoned approach — they're dead code, never activated by any
  `SPRING_PROFILES_ACTIVE` setting in the current compose files; see §9.)

**Required `.env` keys** (names only — see `architecture-reference.md` §12 for what each maps to;
actual values are a secrets concern and must never be committed or pasted anywhere):

```
POSTGRES_DB
KOPA_DB_URL / KOPA_DB_USERNAME / KOPA_DB_PASSWORD / KOPA_DB_DRIVER
SPRING_RABBITMQ_HOST / _PORT / _USERNAME / _PASSWORD
APP_MESSAGING_EXCHANGE

KOPA_BUSINESS_LOGIN_ID / _ORG_ID / _SO_ID
KOPA_KTRN_BASE_URL / _EXTERNAL_BASE_URL / _ISP_CD
KOPA_SMS_INTOUCH_BASE_URL / _USERNAME / _PASSWORD
KOPA_SMS_DEFAULT_SENDER

KOPA_MIDDLEWARE_KTRN_BASE_URL
KOPA_MIDDLEWARE_SMS_BASE_URL
KOPA_NOTIFICATIONS_BASE_URL
KOPA_CUSTOMER_IDENTITY_BASE_URL

KOPA_INTERNAL_MIDDLEWARE_KTRN_API_KEY
KOPA_INTERNAL_MIDDLEWARE_SMS_API_KEY
KOPA_INTERNAL_NOTIFICATIONS_API_KEY
KOPA_INTERNAL_CUSTOMER_IDENTITY_API_KEY
KOPA_INTERNAL_ADMIN_PORTAL_API_KEY

KOPA_WEB_ALLOWED_ORIGINS
```

Each `KOPA_INTERNAL_<X>_API_KEY` is a secret tied to **one target service** — the same value
satisfies both that service's own inbound guard on `/internal/v1/**` and every caller's outbound
`X-Internal-Api-Key` header, since both sides reference the identical property name. This is
deliberately *not* one shared secret for all internal traffic.

---

## 6. Docker deployment

### 6.1 Image build — the same pattern for all six services

Every service's `Dockerfile` is an identical two-stage build:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/<service-name>-0.0.1-SNAPSHOT.jar app.jar
EXPOSE <port>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- Stage 1 resolves dependencies and compiles with full Maven + JDK; stage 2 is a minimal
  `eclipse-temurin:17-jre-alpine` runtime that only ever sees the final jar — no build tooling, no
  source, no Maven cache ships in the runtime image.
- Each service directory has its own `.dockerignore` (`target/`, `.idea/`, `.git/`, `*.md`) so
  `docker build`'s context doesn't drag in prior local build output or IDE files.
- Tests are skipped at image-build time (`-DskipTests`) — this build is for packaging a jar
  that's already been developed/tested locally, not a CI pipeline.

### 6.2 `docker-compose.yml` — one compose project for the whole backend

All six services plus Postgres and RabbitMQ live in a single `backend/docker-compose.yml`,
started together as one compose project:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    restart: unless-stopped
    environment: { POSTGRES_DB, POSTGRES_USER: ${KOPA_DB_USERNAME}, POSTGRES_PASSWORD: ${KOPA_DB_PASSWORD} }
    ports: ["127.0.0.1:5432:5432"]      # localhost only - never exposed beyond the host
    volumes: ["kopa_pgdata:/var/lib/postgresql/data"]
    healthcheck: pg_isready

  rabbitmq:
    image: rabbitmq:4-management-alpine
    restart: unless-stopped
    ports: ["127.0.0.1:5672:5672", "127.0.0.1:15672:15672"]   # localhost only
    healthcheck: rabbitmq-diagnostics ping

  kopa-middleware-ktrn:      # build: ./middleware/kopa.middleware-ktrn, port 8082
  kopa-middleware-sms:       # build: ./middleware/kopa.middleware-sms,  port 8086
  kopa-notifications:        # build: ./microservices/mobile/kopa.notifications, port 8084
  kopa-customer-identity:    # build: ./microservices/mobile/kopa.customer-identity, port 8083
  kopa-customer-self-care:   # build: ./microservices/mobile/kopa.customer-self-care, port 8081

  kopa-admin-portal:         # build: ./microservices/web/kopa.admin-portal, port 8085
    networks: [default, kopa-net]   # <- joins the shared external network, see 6.3

volumes:
  kopa_pgdata:

networks:
  kopa-net:
    external: true
```

Conventions applied uniformly:
- `restart: unless-stopped` on every service — survives host reboots and crashes without manual
  intervention.
- `env_file: .env` on every app service (not Postgres/RabbitMQ, which take their credentials
  directly as compose `environment:` values sourced from the same `.env`).
- `depends_on` with `condition: service_healthy` for Postgres/RabbitMQ, `condition:
  service_started` for inter-service ordering (e.g. `kopa-customer-identity` waits on
  `kopa-middleware-ktrn` and `kopa-notifications` to have *started*, not necessarily finished
  booting — Spring Boot's own retry/connection-pool behavior handles the rest).
- `logging: { driver: json-file, options: { max-size: "10m", max-file: "3" } }` on every service —
  bounds how much disk container logs can consume (30MB max per service) without needing a log
  shipper.
- Postgres and RabbitMQ bind to `127.0.0.1:<port>` only — reachable from the host for
  debugging/admin (e.g. an SSH tunnel to the RabbitMQ management UI) but never exposed on the
  server's real network interface.

### 6.3 The frontend is a *separate* compose project, joined by a shared network

The Next.js admin frontend (`frontend/kopa-telecom-admin/docker-compose.yml`) is deployed as its
**own independent compose project**, not folded into the backend's compose file — deliberately,
to match the server's existing folder layout (`~/backend`, `~/frontend-web`) and let either stack
be rebuilt/restarted/updated without touching the other.

Since `docker compose` normally only gives container-name DNS resolution *within* one compose
project, the two projects share a manually-created external Docker network, `kopa-net`:

```bash
# Run once, before either stack's first `docker compose up`:
docker network create kopa-net
```

Both compose files declare it as `external: true` and both the frontend container and
`kopa-admin-portal` join it — so the frontend can reach the admin backend by container name
(`http://kopa-admin-portal:8085/kopa-admin-portal`) exactly as if they were one compose project,
without opening that port to anything else.

```yaml
# frontend/kopa-telecom-admin/docker-compose.yml
services:
  kopa-admin-frontend:
    build: .
    restart: unless-stopped
    environment:
      ADMIN_PORTAL_API_BASE_URL: http://kopa-admin-portal:8085/kopa-admin-portal
      COOKIE_SECURE: "false"   # plain HTTP, no TLS yet - see §7
    networks: [kopa-net]
    ports: ["3000:3000"]

networks:
  kopa-net:
    external: true
```

The frontend's own `Dockerfile` is a three-stage Node build (`deps` → `build` → `runtime`), all
stages on `node:20-bookworm-slim` rather than an alpine base — the project's lockfile only
carries glibc-built native binaries for `sharp`/`lightningcss` (no musl counterparts), which would
fail to load under Alpine's musl libc. The runtime stage copies only `next.config.ts`'s
`output: "standalone"` bundle (`public/`, `.next/standalone`, `.next/static`) — not the full
`node_modules` tree used to build it.

### 6.4 First-time server setup

```bash
# Install Docker + Compose (if not already present):
curl -fsSL https://get.docker.com | sh
# If apt-get chokes on a stale `file:/cdrom` source left over from an ISO-based OS install:
#   sed -i '/cdrom:/d' /etc/apt/sources.list && apt-get update, then retry the line above.

# Create the shared network used by both compose projects (once, ever):
docker network create kopa-net
```

### 6.5 Shipping code to the server and deploying

Code is pushed from a local machine to the server with `rsync`, into the server's existing
`~/backend` and `~/frontend-web` folders — run from wherever the local repo lives, since `rsync`'s
source/destination are both absolute paths (the command's result doesn't depend on the shell's
current directory):

```bash
rsync -avz --exclude target --exclude .git --exclude logs \
  /path/to/kopa/backend/ root@<server-ip>:~/backend/

rsync -avz --exclude node_modules --exclude .next --exclude .git \
  /path/to/kopa/frontend/kopa-telecom-admin/ root@<server-ip>:~/frontend-web/
```

Then, on the server, build and start each stack independently:

```bash
cd ~/backend
# .env must already exist here with every key from §5 - created once by hand, never rsynced
# from a machine's local kopa-config.yml/plaintext secrets.
docker compose up -d --build

cd ~/frontend-web
docker compose up -d --build
```

Re-running `docker compose up -d --build` after a later `rsync` rebuilds only images whose build
context actually changed and recreates just those containers — the rest keep running.

Verify everything came up:

```bash
docker compose ps                                   # from inside ~/backend or ~/frontend-web
docker logs -f backend-kopa-customer-identity-1 --tail 50   # any service's logs
```

### 6.6 Migrating data from a local Postgres to the server

```bash
# 1. Dump locally (schema + data, custom format):
pg_dump -U <local_user> -d kopa_db -F c -f kopa_db.dump

# 2. Copy the dump to the server:
scp kopa_db.dump root@<server-ip>:~/

# 3. On the server, copy it into the running postgres container and restore:
docker cp ~/kopa_db.dump backend-postgres-1:/tmp/kopa_db.dump
docker exec backend-postgres-1 pg_restore -U <db_user> -d kopa_db --clean --if-exists /tmp/kopa_db.dump
```

`--clean --if-exists` drops conflicting objects before recreating them, so this is safe to re-run
if a migration needs to be redone — but it **will overwrite** whatever is already in the server's
`kopa_db`, so confirm that's actually intended before running it against a server that already has
real data.

---

## 7. Exposing the backend beyond the local network

The server currently has **no public IP of its own** — its only network interface sits on a
private, NAT'd subnet, and the NAT device's public-facing address does not forward any inbound
ports back to it by default. Practical consequence: every service here (ports `8081`–`8086`, and
the frontend's `3000`) is only reachable today by a client that is physically on the same LAN, or
connected to the company VPN that routes into that private subnet. A device merely sharing the
same Wi-Fi SSID is not sufficient if that Wi-Fi itself isn't bridged into the VPN'd network — the
VPN client (or lack of one) on the connecting device is what actually determines reachability.

Options to go beyond that, roughly in order of effort:

1. **VPN client on the connecting device** — simplest, no server-side change, matches how
   everything here already assumes access is gated.
2. **Cloudflare Tunnel** — no inbound firewall rule needed on the server. A *quick* tunnel
   (`cloudflared tunnel --url http://localhost:<port>`) needs no account but issues a new random
   `*.trycloudflare.com` hostname every time the process restarts — fine for ad hoc testing, not
   for anything meant to stay reachable. A stable hostname requires a *named* tunnel, which in
   turn requires owning a domain added to a (free) Cloudflare account.
3. **True inbound port-forwarding** on whatever device owns the NAT's public IP — requires access
   to that device, which may not be something this project controls.

Before exposing any backend port (as opposed to just the frontend) this way, see §9 for the
hardening steps (disabling Swagger/actuator, confirming rate limits on auth/OTP endpoints) that
should happen first — these services were built assuming network-level access control, not
public-internet exposure.

---

## 8. Troubleshooting

- **A service's own logs look completely healthy, but nothing outside the server can reach it** —
  this is almost always §7's reachability issue, not an application bug. Check, in order: is the
  calling device on the VPN/same routed subnet; `ss -tlnp | grep <port>` on the server (confirms
  Docker actually bound `0.0.0.0:<port>`, not just a container-internal address); `ufw status` /
  `iptables -L -n | grep <port>` (host firewall, separate from Docker's own NAT rules).
- **502 / "Could not reach the server" from the frontend's login route** — usually
  `kopa-admin-portal` failed to start (commonly a missing/mistyped `.env` key) or isn't attached
  to `kopa-net`. Check `docker ps -a --filter "name=kopa-admin-portal"`, then
  `docker logs backend-kopa-admin-portal-1 --tail 80`, then `docker network inspect kopa-net` to
  confirm both the backend and frontend containers are listed.
- **Login "succeeds" (200 response) but no session persists** — if the deployment is plain HTTP
  (no TLS/reverse proxy yet), confirm `COOKIE_SECURE=false` is set on the frontend container. A
  `Secure` cookie is silently dropped by the browser on a non-HTTPS origin.
- **Docker's own install script fails on `apt-get update`** — check for a stale `file:/cdrom`
  entry in `/etc/apt/sources.list` (common on an ISO-installed Ubuntu) and remove it before
  retrying.

---

## 9. Known limitations / open items

- Three services (`kopa.customer-identity`, `kopa.customer-self-care`, `kopa.middleware-ktrn`)
  still carry an `application-docker.yaml` from an earlier, abandoned configuration approach.
  It's never activated by the current compose files (no `SPRING_PROFILES_ACTIVE=docker` is set
  anywhere) and its env var names don't match what `.env` actually provides — safe to delete, not
  yet done.
- Application logs write to an ephemeral in-container path (no `LOGS_REPOSITORY` env var is set),
  so they're lost on container recreation. Not yet mounted to a host volume.
- Swagger/OpenAPI (`springdoc.api-docs`/`swagger-ui`) is still enabled by default in every
  service — each one logs its own startup warning about this. Should be disabled before any
  service is ever reachable from the public internet (see §7).
- KTRN's real base URL, Kopa's real business IDs (`ISP_CD`/`SO_ID`/org/login IDs), InTouch SMS
  credentials, and real transaction-limit thresholds are operational/business values supplied by
  Kopa Telecom, not engineering decisions — see `architecture-reference.md` §13 for the full list
  of what's still a placeholder.
- The admin web UI's status-filter dropdown doesn't yet account for lazily-computed `EXPIRED`
  transaction status (a DB row can still read `OTP_PENDING` while its card displays `EXPIRED`
  because the expiry is computed at read time, not written back until the row is next touched) —
  cosmetic filter gap only, not a data-correctness issue.
