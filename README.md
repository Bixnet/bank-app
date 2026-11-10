![CI](https://github.com/Bixnet/bank-app/actions/workflows/ci.yml/badge.svg)

# Bank App

A Java banking application that grew from a learning exercise into a small,
production-style project: a Swing desktop app, a Spring Boot REST API with a
bank-style web UI, login and per-user access, automated tests, CI, and a live
deployment.

**Live:** https://bank-api-rcze.onrender.com (free tier: the first request after a
quiet period can take up to a minute while the server wakes up)

See [ROADMAP.md](ROADMAP.md) for how the project was planned and built, phase by
phase, and [RETRO.md](RETRO.md) for the retrospective.

---

## What it does

- **Web:** sign in or create an account, see your own accounts and the total
  balance, deposit and withdraw. Admins see all accounts.
- **Desktop:** the original Swing app (FlatLaf theme) to manage accounts locally.
- **Account types:** savings (no overdraft) and checking (with overdraft limit).

## Architecture

```
Browser (index.html, login.html, register.html + ES modules)
   │  fetch, session cookie, CSRF token
   ▼
bank-api  (Spring Boot 4, Spring Security)
   ├─ AccountController   GET /accounts, GET /accounts/{n}, POST …/deposit|withdraw
   ├─ AuthController      /api/register, /api/me, /api/csrf   (login/logout via Spring Security)
   ├─ HealthController    /api/health  (for uptime monitoring)
   └─ UserRepository      users, account_owners
   │
   ▼  uses the core code from src/ unchanged
Core (src/bank)            Account model, BankManager, AccountRepository
   │
   ▼
SQLite locally (bank.db)  ·  Postgres on Render (DATABASE_URL)
```

- `src/`, `test/`, `libs/`: core model, service and repository, the Swing GUI,
  and their JUnit tests (built with plain `javac` + the jars in `libs/`).
- `bank-api/`: Spring Boot module. It compiles the core from `../src` (without
  the GUI) and serves the web UI from `src/main/resources/static/`.
- One SQL dialect for both databases (upserts, `DOUBLE PRECISION`).

## Run it

**Web app + API (local, SQLite):**

```
cd bank-api
.\mvnw.cmd spring-boot:run
```

Open http://localhost:8080. To get the demo users (`anna`, `max`, `admin`), set
their passwords in the same terminal **before** starting (PowerShell, single
quotes):

```
$env:DEMO_PASSWORD = 'choose-a-password'
$env:ADMIN_PASSWORD = 'choose-another-password'
```

Or simply create an account on the sign-in page (new users start without
accounts).

**Docker:**

```
docker build -t bank-api .
docker run -p 8080:8080 bank-api
```

**Desktop app:** run `bank.Main` from IntelliJ with the jars in `libs/` on the
classpath, or download `BankApp-v1.0.0-win.zip` from the
[v1.0.0 release](https://github.com/Bixnet/bank-app/releases/tag/v1.0.0).

## Configuration

All settings come from environment variables; nothing secret is in the repo.

| Variable | Purpose |
|---|---|
| `DATABASE_URL` | `postgresql://user:password@host/db`; unset → local SQLite `bank.db` |
| `DEMO_PASSWORD` | creates demo users `anna` and `max` on first start (≥ 8 characters) |
| `ADMIN_PASSWORD` | creates the `admin` user on first start (≥ 8 characters) |
| `SERVER_SERVLET_SESSION_COOKIE_SECURE` | `true` on HTTPS hosting (Render); leave unset locally |
| `PORT` | HTTP port on Render (8080) |

## Security

- Spring Security, **deny by default**; only the page files, sign-in,
  registration and `/api/health` are open.
- Passwords stored only as **BCrypt** hashes; session cookie login
  (HttpOnly, SameSite=Lax, Secure on Render); **CSRF** token on every POST.
- Users see and change only their **own** accounts (403 otherwise); amounts are
  validated on the server (> 0, ≤ 1,000,000, max. 2 decimals).

## Tests and CI

- `bank-api`: `.\mvnw.cmd test` → 28 tests (repository, security rules with
  MockMvc, deposit/withdraw API), all on a throwaway database.
- Core: JUnit 5 tests in `test/`, including the strict deposit/withdraw rules.
- GitHub Actions runs both on every pull request; `main` is protected
  (pull request + 1 approval + green `build-and-test`). SonarCloud analyses
  code quality.

## Hosting

- **Render:** Docker web service (Frankfurt), auto-deploy from `main`, managed
  Postgres. The free database expires on **2026-12-02** unless upgraded.
- **UptimeRobot** checks `/api/health` every 5 minutes.
