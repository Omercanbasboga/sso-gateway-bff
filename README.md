# sso-gateway-bff

A reactive **OAuth2/OIDC-authenticated Backend-for-Frontend (BFF) gateway** built on Spring Cloud Gateway and Spring WebFlux. It sits in front of a single-page frontend and a set of independent backend microservices, handles the browser-facing login/logout flow itself, and forwards each request's OAuth2 access token to the appropriate downstream service.

This is a personal, generic reimplementation of the architecture and patterns from a production BFF gateway I built and operated as part of a larger internal system. It is written from scratch for this repository — no proprietary code, credentials, or infrastructure details from that project are included. It exists as a code sample: the identity-provider details, service names, and routes below are illustrative, not a real deployment.

## What it does

- **Centralizes login** using the OAuth2 **authorization_code** flow (`spring-boot-starter-oauth2-client` + `oauth2Login`) — the SPA never talks to the identity provider directly, only to this gateway.
- **Routes and proxies** requests to independent backend services (a profile API, a session API, a general resource API, and the SPA's own static assets) using Spring Cloud Gateway's declarative route config.
- **Relays the caller's access token** to every downstream route via the `TokenRelay=` filter, so each backend service validates the token itself instead of trusting the gateway blindly.
- **Serves the auth-adjacent pages** (sign-in prompt, sign-out confirmation, 401/unauthorized notice) as server-rendered Thymeleaf templates, so a logged-out or unauthorized user gets a real page instead of a broken SPA route.
- Supports RP-initiated logout against the identity provider (`OidcClientInitiatedServerLogoutSuccessHandler`) in addition to the gateway's own local session logout.
- Also registers a `client_credentials` client (`client_token`) for service-to-service calls the gateway itself needs to make.

## Why a BFF gateway

Putting the OAuth2 client (and its client secret) in a server-side gateway rather than in the SPA avoids ever putting long-lived credentials or refresh tokens in the browser. The frontend only ever holds a same-site session cookie; the gateway holds and relays the actual OAuth2 tokens.

## Tech stack

- Java 17, Spring Boot 3.4
- Spring Cloud Gateway (reactive, WebFlux) — routing, `TokenRelay`
- Spring Security OAuth2 Client (reactive) — `oauth2Login`, OIDC RP-initiated logout
- Thymeleaf — server-rendered auth pages
- Log4j2 — structured logging with size-based rolling

## Configuration

Everything environment-specific is externalized — nothing is hardcoded. Set these via environment variables (see `application-default.yml`):

| Variable | Purpose |
|---|---|
| `OAUTH2_CLIENT_ID` / `OAUTH2_CLIENT_SECRET` | OAuth2 client credentials registered with your identity provider |
| `OAUTH2_ISSUER_URI` | Identity provider's OIDC issuer (used for endpoint discovery) |
| `APP_BASE_URI` | This gateway's own public base URL (used as the OAuth2 redirect URI and post-logout redirect) |
| `UI_URI` | Where the frontend SPA is served from |
| `ALLOWED_ORIGINS` | Comma-separated list of origins allowed by CORS |
| `CORE_API_URI` / `PROFILES_API_URI` / `SESSIONS_API_URI` / `USERINFO_API_URI` | Base URLs of the downstream services this gateway proxies to |

## Running locally

```bash
export OAUTH2_CLIENT_ID=your-client-id
export OAUTH2_CLIENT_SECRET=your-client-secret
export OAUTH2_ISSUER_URI=https://your-identity-provider.example.com
./mvnw spring-boot:run
```

The gateway listens on port `8082` by default.

## Project layout

```
src/main/java/dev/omercanbasboga/ssogateway/
├── SsoGatewayBffApplication.java
├── controller/
│   ├── AuthPagesController.java   # login/logout/401 page routes
│   └── LogoutController.java      # local session logout endpoint
└── security/
    └── SecurityConfig.java        # OAuth2 login, CORS, RP-initiated logout
```
