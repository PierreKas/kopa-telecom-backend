# Kopa Telecom Backend — Architecture & Structure Plan

Structure only — no code in this document. Follows the exact conventions already proven in the
DigiSanté backend (`architecture-reference.md`, `db-diagram.md`, `digi.sante.notifications/README.md`):
independent Spring Boot services, one Postgres instance with one schema per service, RabbitMQ for
async cross-service reactions, read-only cross-schema mirrors instead of internal REST wherever a
service just needs to *read* another's data, and a small internal-REST surface only where an
immediate answer is genuinely required.

## Table of contents

1. [The big picture](#1-the-big-picture)
2. [Core principles carried over from DigiSanté](#2-core-principles-carried-over-from-digisante)
3. [Client vs Admin separation](#3-client-vs-admin-separation)
4. [The services](#4-the-services)
5. [Mobile-side microservices — responsibilities & endpoints](#5-mobile-side-microservices--responsibilities--endpoints)
6. [Web-side microservice — kopa.admin-portal](#6-web-side-microservice--kopaadmin-portal)
7. [Middleware layer](#7-middleware-layer)
8. [Cross-service communication](#8-cross-service-communication)
9. [Traceability: correlation IDs, structured logs, failure layers](#9-traceability-correlation-ids-structured-logs-failure-layers)
10. [Transaction limits](#10-transaction-limits)
11. [Standard internal layout of a service](#11-standard-internal-layout-of-a-service)
12. [Configuration & secrets](#12-configuration--secrets)
13. [Open items / assumptions](#13-open-items--assumptions)

---

## 1. The big picture

```
                         ┌───────────────────────────┐
                         │   Client (iOS/Android)     │
                         └──────────────┬─────────────┘
                                        │ JSON, /api/v1/**
              ┌─────────────────────────┼─────────────────────────┐
              ▼                         ▼                         ▼
     kopa.customer-identity   kopa.customer-self-care     kopa.notifications
       (8083)                   (8081)                      (8084)
              │                         │                         │
              └───────────┬─────────────┘         sync OTP send   │
                          │ read-only mirror                      │
                          │ (auth_tokens, customers)               │
                          ▼                                       ▼
                 kopa.middleware-ktrn (8082)              kopa.middleware-sms (8086)
                          │                                       │
                          ▼                                       ▼
                  KTRN / RBSS-CCBS BSS                    InTouch SMS Gateway
                  (VPN-only, www.4glte.rw)                (public internet)

     ┌───────────────────────────┐
     │  Admin (Web Portal)       │
     └──────────────┬─────────────┘
                    │ JSON, /api/v1/admin/**
                    ▼
           kopa.admin-portal (8085)  ── read-only mirrors ──▶  every schema above
                    │
                    ▼
              (own auth, own schema, never shares login with Mobile)

     All six services share ONE Postgres instance (one schema each) and ONE RabbitMQ
     topic exchange (kopa.events) for async cross-service reactions.
```

No API gateway, no shared JAR between services — each is a complete, independently deployable
Spring Boot app, exactly like DigiSanté.

---

## 2. Core principles carried over from DigiSanté

These are the same four rules from DigiSanté's `architecture-reference.md` §2, applied here:

1. **No shared library.** If two services need the same shape (an event, a read-only entity
   mirror), each defines its own local copy. Independently deployable > DRY across service
   boundaries.
2. **Prefer async events over synchronous calls.** Default is "publish an event, don't wait."
   Sync internal REST is the exception, used only when the caller genuinely cannot proceed
   without an immediate answer (OTP dispatch is the main case here — see §8.2).
3. **One Postgres instance, one schema per service**, each with its own Flyway history table
   name (`flyway_schema_history_<service>`).
4. **Cross-service reads use a local read-only mirror entity**, not an internal REST call — the
   *one* documented exception to "no shared library," used for token validation (every Mobile
   service mirrors `customer_identity.customers` / `.auth_tokens`) and for Admin's reporting
   (mirrors into every other schema, read-only, never written).

---

## 3. Client vs Admin separation

Per your explicit requirement: **Client (Mobile) and Admin (Web) are fully separate microservices,
with separate authentication, never mixed in the same service or the same controller.**

- Mobile side: `kopa.customer-identity`, `kopa.customer-self-care`, `kopa.notifications` — all
  authenticate subscribers via `customer_identity.auth_tokens` (bearer token, MSISDN+PIN login).
- Web side: `kopa.admin-portal` — its own `admin_users` / `admin_auth_tokens`, completely
  independent credential space. An admin login can never resolve a Mobile bearer token and
  vice versa.
- `kopa.admin-portal` never writes to any Mobile-side schema. It reads them (read-only mirrors,
  §2 rule 4) purely for dashboards/reports (FR-14).
- Both Middleware services (`kopa.middleware-ktrn`, `kopa.middleware-sms`) are shared
  infrastructure underneath both sides — they don't know or care whether the caller was Mobile
  or Web, they just broker the 3rd-party protocol.

---

## 4. The services

| Service | Port | Context path | Schema | Layer | Owns |
|---|---|---|---|---|---|
| **kopa.customer-identity** | 8083 | `/kopa-customer-identity` | `customer_identity` | Microservices (Mobile) | Registration/activation, login, PIN, session, profile |
| **kopa.customer-self-care** | 8081 | `/kopa-customer-self-care` | `customer_self_care` | Microservices (Mobile) | Balance, usage, bundles, recharge, statement, transaction limits |
| **kopa.notifications** | 8084 | `/kopa-notifications` | `notifications` | Microservices (Mobile-triggered) | SMS orchestration, delivery tracking, send-log |
| **kopa.admin-portal** | 8085 | `/kopa-admin-portal` | `admin_portal` | Microservices (Web) | Admin accounts, RBAC, dashboards/reports |
| **kopa.middleware-ktrn** | 8082 | `/kopa-middleware-ktrn` | *(none — stateless)* | Middleware | All 18 KTRN/RBSS-CCBS operations |
| **kopa.middleware-sms** | 8086 | `/kopa-middleware-sms` | *(none — stateless)* | Middleware | InTouch SMS send + delivery webhook |

`kopa.customer-self-care` and `kopa.middleware-ktrn` already exist (built for feature 2.1.1) and
keep their current ports/paths unchanged — everything else is new, numbered around them.

### 4.1 Folder layout — layer is visible in the path, not just the port

```
kopa/backend/
├── kopa-config.yml              # shared, NOT committed - see s12
├── microservices/
│   ├── mobile/                  # Client-facing, own auth (customer_identity.auth_tokens)
│   │   ├── kopa.customer-identity/     (built)
│   │   ├── kopa.customer-self-care/    (built)
│   │   └── kopa.notifications/         (not built yet)
│   └── web/                     # Admin-facing, own auth (admin_portal.admin_auth_tokens)
│       └── kopa.admin-portal/          (not built yet)
├── middleware/                  # Stateless 3rd-party protocol bridges, shared by both sides
│   ├── kopa.middleware-ktrn/           (built)
│   └── kopa.middleware-sms/            (not built yet)
└── tools/                       # dev-only helpers (e.g. mock_ktrn_server.py), not a service
```

The Client/Admin separation from §3 is now structural, not just a naming convention — a service
under `microservices/web/` physically cannot be mistaken for one under `microservices/mobile/`.

---

## 5. Mobile-side microservices — responsibilities & endpoints

### 5.1 kopa.customer-identity

Owns everything about *who the subscriber is* — the Kopa equivalent of DigiSanté's
`auth-access`. Every other Mobile service mirrors its `customers`/`auth_tokens` tables
read-only for token validation (§2 rule 4), instead of calling it over HTTP per request.

**FRs covered:** FR-01 (registration/activation), FR-02 (login), FR-03 (PIN management), FR-04
(OTP orchestration entry point), FR-12 (profile), FR-15 (session handling).

| Method | Path | Purpose | KTRN op used |
|---|---|---|---|
| GET | `/api/v1/registration/msisdn-status` | Check if an MSISDN is available | HP00005/HPOP601 |
| GET | `/api/v1/registration/msisdn-suggestions` | Recommend 5 MSISDNs for a new line | HP00005/HPOP602 |
| POST | `/api/v1/registration/price-plans` | Price plans available for simple activation | HP00005/HPOP301 |
| POST | `/api/v1/registration/verify-nida` | KYC: citizen lookup (Rwandan nationals) | HP00005/HPOP303 |
| POST | `/api/v1/registration/verify-passport` | KYC: foreigner lookup (DGIE) | HP00005/HPOP306 |
| POST | `/api/v1/registration/verify-rra` | KYC: company lookup by TIN (corporate onboarding) | HP00005/HPOP305 |
| POST | `/api/v1/registration/activate` | Simple activation + subscribe customer, orchestrated | HP00005/HPOP302, HP00005/HPOP501 |
| POST | `/api/v1/auth/login` | MSISDN + 6-digit PIN → bearer token | *(local only)* |
| POST | `/api/v1/auth/logout` | Revoke current token | *(local only)* |
| POST | `/api/v1/auth/pin/set` | First PIN set-up, post-activation | *(local only)* |
| POST | `/api/v1/auth/pin/change` | Change PIN (requires current PIN) | *(local only)* |
| POST | `/api/v1/auth/pin/reset/request` | Sends OTP for PIN reset (**sync** call to Notifications) | *(local + Notifications)* |
| POST | `/api/v1/auth/pin/reset/confirm` | Verify OTP, set new PIN | *(local only)* |
| GET | `/api/v1/profile/me` | View own profile | *(local, optionally HP00005/HPOP502 cross-check)* |
| PUT | `/api/v1/profile/me` | Update editable profile fields | *(local only)* |

Publishes (async, `kopa.events` exchange): `customer.activated` → Notifications sends a welcome
SMS. `customer.registration-failed` → informational, no consumer yet.

### 5.2 kopa.customer-self-care

Already exists — feature 2.1.1 (`GET /api/v1/balance/{msisdn}`) lives here. This section is the
**full** planned surface it grows into.

**FRs covered:** FR-05 (balance), FR-06 (usage), FR-07 (browse bundles), FR-08 (purchase
bundle), FR-09 (recharge), FR-10 (statement).

| Method | Path | Purpose | KTRN op used |
|---|---|---|---|
| GET | `/api/v1/balance/{msisdn}` | Balance search — **feature 2.1.1, built** | HP00001/HPOP001 |
| GET | `/api/v1/balance/{msisdn}/summary` | Richer balance + usage + bundle history in one call | HP00005/HPOP101 |
| GET | `/api/v1/usage/{msisdn}` | Usage/remaining allowance for active bundles | HP00001/HPOP008 |
| GET | `/api/v1/bundles/{msisdn}` | Bundles available for purchase | HP00001/HPOP005 |
| POST | `/api/v1/bundles/purchase` | Purchase a bundle (limit-checked, OTP-gated) | HP00005/HPOP402, HPOP403, HP00002/HPOP004 |
| POST | `/api/v1/recharge` | Recharge by amount (limit-checked, OTP-gated) | HP00002/HPOP001 |
| POST | `/api/v1/recharge/voucher` | Recharge by voucher number | HP00002/HPOP003 |
| POST | `/api/v1/recharge/{transactionId}/cancel` | Cancel a recharge | HP00002/HPOP002 |
| POST | `/api/v1/transactions/{id}/otp/verify` | Complete an OTP-gated transaction | *(local, then proceeds to the KTRN call above)* |
| GET | `/api/v1/statement/{msisdn}` | Purchase/recharge/usage history | Local `transactions` table + HP00005/HPOP101's `BUNDLE_HIST` |

Reads `customer_identity.customers` / `.auth_tokens` read-only for token validation.

Calls `kopa.notifications` **synchronously** before finalizing a limit-checked, OTP-gated
transaction (client is waiting on the response to know "OTP sent, show me the entry field").

Publishes (async): `transaction.completed`, `transaction.failed` → Notifications sends a
confirmation/failure SMS. Neither blocks the client's HTTP response.

### 5.3 kopa.notifications

The Kopa equivalent of `digi.sante.notifications` — "no domain data of its own, it's a
send-log," triggered by events from the other two Mobile services, sitting on top of
`kopa.middleware-sms`.

**FR covered:** FR-11 (transactional SMS notifications).

| Method | Path | Purpose | Caller |
|---|---|---|---|
| POST | `/internal/v1/otp/send` | **Sync.** Sends an OTP, returns immediately with success/failure | customer-identity, customer-self-care |
| POST | `/internal/v1/sms/delivery-status` | **Sync.** Receives a forwarded DLR event from middleware-sms | kopa.middleware-sms only |

Consumes (async, `kopa.events`): `customer.activated`, `transaction.completed`,
`transaction.failed`.

All sends — OTP or event-triggered — go through the same internal `SmsDispatchService`, which
calls `kopa.middleware-sms`'s `/internal/v1/sms/send`, then writes one row to `sms_messages`.

---

## 6. Web-side microservice — kopa.admin-portal

**FRs covered:** FR-13 (admin login/RBAC), FR-14 (admin monitoring/reports).

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/admin/auth/login` | Admin login (own credential space) |
| POST | `/api/v1/admin/auth/logout` | Revoke admin session |
| GET | `/api/v1/admin/customers` | Search/list customers (mirrors `customer_identity.customers`) |
| GET | `/api/v1/admin/customers/{id}` | Customer detail |
| GET | `/api/v1/admin/transactions` | Search/list transactions (mirrors `customer_self_care.transactions`) |
| GET | `/api/v1/admin/transactions/{id}` | Transaction detail, including `failure_layer`/`failure_detail` if it failed |
| GET | `/api/v1/admin/sms-messages` | SMS send/delivery history (mirrors `notifications.sms_messages`) |
| GET | `/api/v1/admin/dashboard/summary` | Today's registrations, transactions, SMS delivery rate |
| GET | `/api/v1/admin/dashboard/failures` | Failure breakdown by layer/service, for support triage |

No writes to any Mobile-side schema, ever — every table above is a read-only local mirror
entity, same mechanism DigiSanté uses for `identity_tenant.users` (`architecture-reference.md`
§5.2).

---

## 7. Middleware layer

Both Middleware services stay **stateless — no Postgres schema of their own** (matches the
feature-2.1.1 decision: persistence belongs to the Microservices layer, Middleware is a pure
protocol bridge). Each is its own Spring Boot app with thin controllers and the
Service/ServiceImpl skeleton from your screenshot: build the request payload the 3rd party
expects, call a shared API-call helper, parse the response.

### 7.1 kopa.middleware-ktrn — modularized by KTRN's own section numbering

`kopa.middleware-ktrn` already exists with one module (`balance/`, HP00001/HPOP001). It grows by
adding one module per KTRN business area — same package shape, same
Service+ServiceImpl-builds-XML pattern, all sharing the existing `KtrnApiClient` (the "API_CALL"
helper) and `KtrnProperties`.

| Module | Internal endpoint(s) | KTRN op(s) |
|---|---|---|
| `balance/` *(exists)* | `POST /internal/v1/ktrn/balance/search` | HP00001/HPOP001 |
| `balance/` *(add)* | `POST /internal/v1/ktrn/balance/summary` | HP00005/HPOP101 |
| `usage/` | `POST /internal/v1/ktrn/usage/search` | HP00001/HPOP008 |
| `bundle/` | `POST /internal/v1/ktrn/bundles/available` | HP00001/HPOP005 |
| `bundle/` | `POST /internal/v1/ktrn/bundles/purchase` | HP00002/HPOP004 |
| `bundle/` | `POST /internal/v1/ktrn/bundles/purchase-check` | HP00005/HPOP403 |
| `bundle/` | `POST /internal/v1/ktrn/bundles/ewallet-check` | HP00005/HPOP402 |
| `recharge/` | `POST /internal/v1/ktrn/recharge` | HP00002/HPOP001 |
| `recharge/` | `POST /internal/v1/ktrn/recharge/cancel` | HP00002/HPOP002 |
| `recharge/` | `POST /internal/v1/ktrn/recharge/voucher` | HP00002/HPOP003 |
| `registration/` | `POST /internal/v1/ktrn/registration/subscribe-customer` | HP00005/HPOP501 |
| `registration/` | `POST /internal/v1/ktrn/registration/search-customer` | HP00005/HPOP502 |
| `registration/` | `POST /internal/v1/ktrn/registration/search-msisdn-by-iccid` | HP00001/HPOP003 |
| `activation/` | `POST /internal/v1/ktrn/activation/price-plans` | HP00005/HPOP301 |
| `activation/` | `POST /internal/v1/ktrn/activation/simple-activate` | HP00005/HPOP302 |
| `identity/` | `POST /internal/v1/ktrn/identity/verify-nida` | HP00005/HPOP303 |
| `identity/` | `POST /internal/v1/ktrn/identity/verify-rra` | HP00005/HPOP305 |
| `identity/` | `POST /internal/v1/ktrn/identity/verify-passport` | HP00005/HPOP306 |
| `msisdn/` | `GET /internal/v1/ktrn/msisdn/status` | HP00005/HPOP601 |
| `msisdn/` | `GET /internal/v1/ktrn/msisdn/suggestions` | HP00005/HPOP602 |

**One wrinkle worth flagging now:** the `identity/` module's three ops (NIDA, RRA, DGIE) use a
**different Request URI** than everything else —
`http://www.4glte.rw/voucher/api/external_access.jsp`, not `/api_access.jsp`. `KtrnProperties`
needs a second `external-base-url` alongside the existing `base-url` for this reason.

### 7.2 kopa.middleware-sms — new

| Endpoint | Direction | Purpose |
|---|---|---|
| `POST /internal/v1/sms/send` | Inbound from `kopa.notifications` | Builds the InTouch JSON payload, HTTP Basic Auth, POSTs to `https://www.intouchsms.co.rw/api/sendsms/.json`, returns `{messageId, status, cost}` |
| `GET /webhooks/intouch/dlr` | Inbound from InTouch (public internet) | Receives `?messageid=&status=`, forwards to `kopa.notifications`'s `/internal/v1/sms/delivery-status`, **always returns 200 OK** per the InTouch spec regardless of the forward's outcome (logged if it fails) |

**Operational note:** unlike every other endpoint in this whole platform, the DLR webhook must
be reachable from the public internet (InTouch calls it, not us) — it needs its own ingress rule
distinct from the VPN-only KTRN traffic and the otherwise fully internal `/internal/v1/**`
surface everywhere else.

---

## 8. Cross-service communication

### 8.1 Async: RabbitMQ (default choice)

One topic exchange, **`kopa.events`** (configurable via `app.messaging.exchange`, matching
DigiSanté's `ds.events` pattern exactly). Routing key = `<subject>.<past-tense-verb>`.

| Routing key | Published by | Consumed by | Effect |
|---|---|---|---|
| `customer.activated` | customer-identity | notifications | Welcome SMS |
| `customer.registration-failed` | customer-identity | *(none yet — informational)* | — |
| `transaction.completed` | customer-self-care | notifications | Confirmation SMS |
| `transaction.failed` | customer-self-care | notifications | Failure SMS |

Each consumer gets its own durable queue (`notifications.customer.activated`, etc.) bound to
`kopa.events`, and deserializes the raw AMQP `Message` manually with its own local copy of the
event record — same reason as DigiSanté (§7.1 of the DigiSanté doc): a typed binding resolves
`__TypeId__` against the *publisher's* class, which doesn't exist on the consumer's classpath,
and loops forever on nack/redeliver.

### 8.2 Sync: internal REST (the exception)

Used only where the caller cannot proceed without an immediate answer:

- **customer-identity / customer-self-care → notifications** (`/internal/v1/otp/send`): the
  client's HTTP response depends on knowing the OTP was actually dispatched.
- **customer-identity / customer-self-care → middleware-ktrn**: every KTRN operation is
  inherently synchronous — the client is waiting on the balance/bundle/recharge result.
- **notifications → middleware-sms** (`/internal/v1/sms/send`): needs the provider's
  `messageId`/`cost` back immediately to log it.
- **middleware-sms → notifications** (`/internal/v1/sms/delivery-status`): forwarding a webhook
  event, needs to know if it landed.

All `/internal/v1/**` paths are excluded from each service's normal Client/Admin bearer-token
requirement and instead require a shared `X-Internal-Api-Key` header (one shared secret per
calling pair, defined in the not-committed secrets file — §12). Always set connect+read timeouts
on these clients — same rule as DigiSanté §7.2, never let one service's slowness cascade.

### 8.3 Read-only mirrors (the other exception)

- Every Mobile service that validates a bearer token mirrors `customer_identity.customers` /
  `.auth_tokens`, read-only, exactly like DigiSanté's `auth_access.accounts`/`auth_tokens`
  pattern.
- `kopa.admin-portal` mirrors into `customer_identity`, `customer_self_care`, and
  `notifications` for its reporting endpoints — read-only, never written.

---

## 9. Traceability: correlation IDs, structured logs, failure layers

This directly answers "if a transaction fails, we should know immediately whether it's KTRN,
Middleware, our DB, or which Microservice, and why."

### 9.1 Correlation ID, propagated everywhere

Every service gets a `CorrelationIdFilter` (`common/trace/`, `@Order(1)`, runs before
everything else):
- Reads `X-Correlation-Id` from the incoming request, or generates a new UUID if absent.
- Puts it in SLF4J MDC as `correlationId`.
- Echoes it back on the response header.
- Every outbound `RestClient` call (Microservice → Middleware, Middleware → 3rd party is
  logged with it too even though the 3rd party won't echo it back) forwards it as
  `X-Correlation-Id`.

### 9.2 Logback pattern, extended with the correlation ID

Same `logback-spring.xml` shape as DigiSanté §10 (one shared log directory, one rolling file per
service, distinguished by `SERVICE_NAME`), with one addition to the pattern:

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{correlationId}] %-5level %logger{36} - %msg%n
```

Grepping one correlation ID across every service's log file in the shared directory reconstructs
the full path a single request took, layer by layer.

### 9.3 `failure_layer` — persisted, not just logged

A shared enum (defined locally in each service that needs it, per the no-shared-library rule):

```
NONE, VALIDATION, MICROSERVICE, MIDDLEWARE, KTRN_GATEWAY, KTRN_BUSINESS,
SMS_GATEWAY, SMS_BUSINESS, DATABASE
```

- `VALIDATION` — request never left the Microservice (bad input, limit exceeded).
- `MICROSERVICE` — a bug/exception inside the Microservice's own logic.
- `MIDDLEWARE` — the Microservice→Middleware internal call itself failed (timeout, 5xx).
- `KTRN_GATEWAY` / `SMS_GATEWAY` — Middleware couldn't reach the 3rd party at all (DNS,
  connection refused, timeout — exactly what we hit debugging feature 2.1.1).
- `KTRN_BUSINESS` / `SMS_BUSINESS` — the 3rd party answered, but with an error (KTRN msgcode
  ≠ 1001, InTouch `success: false`).
- `DATABASE` — Postgres itself (constraint violation, connection pool exhausted).

Every table that records an attempt (`transactions`, `sms_messages`, `activation_requests`,
`kyc_verification_log`) carries `failure_layer` + `failure_detail` + `correlation_id` columns
(see `db-diagram.md`). Reading one row tells you where it broke without touching logs — logs are
for the *why* (full stack trace), the DB row is for the *where*, at a glance, from the Admin
dashboard's failure-breakdown endpoint (§6).

---

## 10. Transaction limits

Lives in `kopa.customer-self-care` (where recharge/purchase happen), enforced *before* calling
Middleware:

- **`transaction_limit_policy`** (schema table): per `transaction_type` —
  `max_amount_per_tx`, `max_amount_per_day`, `max_count_per_day`, `active`. Configurable, not
  hardcoded — the Scope of Work states limits are a Kopa-provided business rule, so this table
  is the mechanism, not the real numbers (placeholders until Kopa confirms them).
- **`daily_transaction_counter`** (schema table): one row per `(msisdn, date, transaction_type)`,
  incremented atomically as transactions post, checked against the policy before a new one is
  allowed through.
- Enforcement point: a `TransactionLimitService`, called at the top of the recharge/purchase
  service methods. A breach throws a typed `TransactionLimitExceededException` → `429`, logged
  with `failure_layer = VALIDATION` — the request never reaches Middleware/KTRN at all.

---

## 11. Standard internal layout of a service

Same shape as DigiSanté §8, copied verbatim per new service:

```
src/main/java/com/kopa/<service>/
├── Application.java
├── config/            # Consts, AppConfig, DatabaseProperties, SwaggerConfig, CorrelationIdFilter
├── common/
│   ├── dto/            # ApiResponse<T>, ErrorDto, FieldErrorDto
│   ├── exception/       # ApiException hierarchy + GlobalExceptionHandler
│   └── trace/           # CorrelationIdFilter, FailureLayer enum
├── auth/                # read-only mirror of customer_identity.{customers,auth_tokens}
│                        # (Mobile services only; admin-portal has its own local auth instead)
├── <domain>/             # one package per bounded concern this service owns
│   ├── controller/
│   ├── dto/
│   ├── model/
│   ├── repository/
│   └── service/ (+ impl/)
├── messaging/            # EventPublisher, RabbitConfig, event/, listener/ (services that need it)
└── resources/
    ├── application.yaml
    ├── application-docker.yaml
    ├── logback-spring.xml
    └── db/migration/
```

Middleware services drop `auth/`, `messaging/`, and any `db/migration/` (stateless) but keep
everything else, with `<domain>/service/` holding the Service+ServiceImpl skeleton pattern.

---

## 12. Configuration & secrets

Same four-layer scheme as DigiSanté §9, **implemented** (not just planned) for all three built
services:

1. `application.yaml` per service — imports the one shared, **not committed**, `kopa-config.yml`
   sitting at `kopa/backend/` root:
   ```yaml
   spring:
     config:
       import: optional:file:/.../kopa/backend/kopa-config.yml
   ```
   The `optional:` prefix means a missing file (e.g. inside a container) is silently skipped
   rather than failing startup.
2. `kopa-config.yml` — real local values: DB url/username/password (`kopa.db.*`), KTRN
   base-url(s) and ISP_CD (`kopa.ktrn.*`), Kopa's own KTRN-assigned business ids
   (`kopa.business.*`), each Microservice's Middleware base-url (`kopa.middleware.*-base-url`),
   the shared log directory (`kopa.logs.repository`), CORS origins (`kopa.web.allowed-origins`),
   and RabbitMQ connection details. One file, every service reads from it — not one env-var
   default per service like the very first feature-2.1.1 pass had.
3. `application-docker.yaml` per service — the same property names, sourced from environment
   variables instead, activated via `SPRING_PROFILES_ACTIVE=docker`. No `docker-compose.yml`
   exists yet; this just establishes the pattern ahead of time.
4. `docker-compose.yml` + `.env` at deploy time (not built yet).

### 12.1 The DataSource/Flyway bean — not `spring.datasource.*`

Every service with a schema (`kopa.customer-identity`, `kopa.customer-self-care`) binds a local
`DatabaseProperties` (`@ConfigurationProperties(prefix = "kopa.db")`) and builds its `DataSource`
and `Flyway` beans manually in `AppConfig`, exactly like `digi.sante.auth-access.AppConfig` —
**not** Spring Boot's automatic `spring.datasource.*`/`spring.flyway.*` binding:

```java
@Primary @Bean
public DataSource dataSource() {
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName(databaseProperties.getDriver());
    ds.setUrl(databaseProperties.getUrl());
    ds.setUsername(databaseProperties.getUsername());
    ds.setPassword(databaseProperties.getPassword());
    return ds;
}

@Bean(initMethod = "migrate")
public Flyway flyway(DataSource dataSource) {
    return Flyway.configure().dataSource(dataSource)
            .schemas("customer_self_care").defaultSchema("customer_self_care")
            .table("flyway_schema_history_customer_self_care")   // <- distinct per service
            .load();
}
```

Why the distinct Flyway history table name: both services share **one physical Postgres
database** (`kopa_db`), so Flyway's own bookkeeping table would collide across services if left
at its default name — same reasoning as DigiSanté §5.1. A `NoOpDataSource` stand-in (same as
DigiSanté's) exists for `-DskipDatabase=true` test runs that shouldn't need a real DB.

Middleware services (`kopa.middleware-ktrn`) have no schema and skip this entirely — they still
import `kopa-config.yml` for their own settings (`kopa.ktrn.*`), just no `DatabaseProperties`.

**Not yet replicated from DigiSanté:** the `ENC(...)` AES password-encryption wrapper around
`kopa.db.password` (DigiSanté §9.1). `kopa-config.yml`'s DB password is plaintext for now — add
if/when that's actually wanted, it's a small addition once `AppConfig` already exists.

---

## 13. Open items / assumptions

- **KTRN real base-url/port** — currently unreachable even from inside Kopa's network (see
  feature 2.1.1 debugging). Escalated to Kopa Telecom; `ktrn.base-url` stays
  environment-overridable.
- **KTRN's `external_access.jsp` host** — same open question for the NIDA/RRA/DGIE ops, once
  reached.
- **Kopa's real ISP_CD / SO_ID / business numbers** — still placeholders (`62` from the doc's
  examples).
- **InTouch credentials** — not yet provisioned; `kopa.middleware-sms` needs them in
  `kopa-config.yml` before it can send anything real.
- **Transaction limit real values** — mechanism is designed, actual thresholds are a Kopa
  business decision per the Scope of Work.
- **RabbitMQ** — new infrastructure dependency for this project (Postgres was already in use;
  RabbitMQ wasn't provisioned before this plan). `kopa.customer-identity`'s `customer.activated`
  and `kopa.customer-self-care`'s `transaction.completed`/`.failed` events aren't publishable
  until it exists.
- **Auth is now implemented in `kopa.customer-identity`**: MSISDN+PIN login, PIN set/change,
  session issuance/validation via `TokenValidationFilter` + `CallerContext`, full
  KTRN-backed registration/activation. **PIN reset (forgot PIN) is still deferred** — it needs an
  OTP sent via `kopa.notifications`, which doesn't exist yet. `kopa.customer-self-care`'s own
  endpoints still have no bearer-token requirement — it doesn't yet mirror
  `customer_identity.{customers,auth_tokens}` read-only per §8.3; that's the next piece needed
  before its endpoints can require a session.
