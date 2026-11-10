# Bank App — Professional Software Engineering Roadmap

This is the master plan for turning the Bank app from a learning exercise into a
real, deployed, professionally-managed piece of software. Every phase below maps
to what actual software teams do. We follow this in order — nothing gets skipped.

**Team setup:** You play two roles — Developer and Product Owner/Scrum Master
(using a second account so the workflow feels real: someone assigns tickets,
someone picks them up).

**Budget:** Free tiers everywhere possible; a few dollars/month only where
strictly needed.

---

## Standing Team Workflow ✅ ACTIVE

Two GitHub accounts, mirroring the Jira Developer/Product Owner setup:
- **Bixnet** — Developer: writes code, opens Pull Requests, merges once approved
- **staffordjunior910-max** — Collaborator: reviews and approves / requests changes

`main` is protected by a GitHub Ruleset: pull request required, 1 approval, and
the `build-and-test` check must pass. Direct pushes are rejected.

The cycle for every change:
```
git checkout main; git pull; git checkout -b feature/some-change
# ...make changes...
git add <paths>; git commit -m "Describe the change"; git push -u origin feature/some-change
```
Open the PR → reviewer approves → CI green → **Merge pull request** → **Delete branch** →
`git checkout main; git pull; git branch -d feature/some-change`.
Review fixes go on the **same branch**. Every ticket exists in Jira before coding starts.

---

## Phase 0 — Foundations & Tooling Setup ✅ COMPLETE
*Goal: every tool a real dev team uses is installed and connected before any new code is written.*

**Status: repo created and pushed to `github.com/Bixnet/bank-app`; Jira Kanban
board set up with a second (Product Owner) account; existing work logged as
Done tickets. Note: Jira and this file are NOT connected to each other or to
GitHub — each is updated by hand, which is why this file needs a manual
update like this one whenever something is finished.**

### 0.1 — Create the GitHub repository ✅
1. Go to https://github.com/signup and create an account if you don't have one (this is "you, the Developer").
2. Click **+** (top-right) → **New repository**.
3. Name: `bank-app`. Description: "Java banking application — learning project → production-style build."
4. Visibility: **Public** (needed later for SonarCloud's free tier and some other tools).
5. Do **NOT** check "Add a README" or "Add .gitignore" — we already have both locally; checking those causes push conflicts.
6. Click **Create repository** and copy the URL it gives you (`https://github.com/yourusername/bank-app.git`).

### 0.2 — Connect the local project and push the first commit ✅
In IntelliJ, with the `Bank` project open:
1. **VCS → Enable Version Control Integration → Git** (skip if already enabled).
2. **VCS → Git → Remotes** → **+** → paste the GitHub URL from 0.1 → name it `origin` → OK.
3. **VCS → Commit** (`⌘K`) — select all files (`src/`, `.gitignore`, `Bank.iml`, `lib/`).
4. Commit message:
   ```
   Initial working bank app: interfaces, inheritance, exceptions, collections, GUI, database
   ```
5. Click **Commit** (not "Commit and Push" — commit first, push separately so each step is visible).
6. **VCS → Git → Push** (`⌘⇧K`) → confirm target is `origin/master` → **Push**.
7. Refresh the GitHub repo page in your browser — all files should now appear there.

This is commit #1 — the entire current app, as one snapshot.

### 0.3 — Set up Jira with a Kanban board ✅
1. Go to https://www.atlassian.com/software/jira → **Get it free** → sign up.
2. When asked what you're building: **Software project** → template **Kanban** (simpler for solo/two-person use; we can layer Scrum sprints in later once there are real tickets).
3. Name the project `Bank App`.
4. You'll land on a board with columns **To Do → In Progress → Done** — leave it empty for now; tickets get created properly in Phase 1 so they map to real work instead of being guessed upfront.

### 0.4 — Second account (simulated Product Owner) ✅
1. Open a private/incognito browser window, or use a second email address.
2. Sign up for a **separate Jira account** with that email.
3. From your first (Developer) account: **Project Settings → People** → invite that second email as a member.
4. Accept the invite from the second account.

Result: two logins — "Developer" (writes code, picks up tickets) and "Product Owner" (creates tickets, reviews, moves cards to Done). This makes the workflow feel real: tickets get *assigned to you*, not self-assigned.

### 0.5 — Log existing work as completed history ✅
Once the board exists, create these as tickets and immediately mark all **Done** (this isn't new work — it's making the board reflect what's already built):
- `Implement Account interface + inheritance hierarchy`
- `Add abstract Account class + polymorphism`
- `Add custom exception handling`
- `Add BankManager with collections`
- `Build Swing GUI`
- `Add SQLite persistence`
- `Apply FlatLaf theme + input validation + logging`

**Commit point:** after 0.2 — this is commit #1, the whole current codebase.

---

## Phase 1 — Backlog & Sprint Planning ✅ COMPLETE
*Goal: work is tracked like a real sprint, not ad-hoc chat requests.*

- [x] 1.1 Write epics: `Testing`, `CI/CD`, `Code Quality`, `Packaging`, `Deployment` (Jira: BA-8 to BA-12)
- [x] 1.2 Break epics into stories/tickets with acceptance criteria (BA-13, BA-14, BA-15 under Testing)
- [x] 1.3 Sprint 1 scope = the 3 Testing tickets (Kanban board, no formal sprint feature used)
- [x] 1.4 Tickets moved across the board as work happened

**Commit point:** none — this phase is planning only, no code changes.

---

## Phase 2 — Automated Testing ✅ COMPLETE
*Goal: prove the app works with tests, not just manual clicking.*

- [x] 2.1 Add JUnit 5 as a dependency (`junit-platform-console-standalone-1.10.2.jar` — the combined jar; avoid mixing in IntelliJ's auto-added JUnit4/5.8.1 libraries, they collide)
- [x] 2.2 Unit tests for `SavingsAccount` (4 tests), `CheckingAccount` (3 tests) — deposit/withdraw logic, exception thrown correctly
- [x] 2.3 Unit tests for `BankManager` (5 tests) — add/find/deposit/withdraw via the manager
- [x] 2.4 Full suite run locally — 12/12 passing

**Known gap logged for later:** tests currently write to the real `bank.db` instead of an isolated test database — tracked as its own backlog ticket, not blocking.

---

## Phase 3 — Continuous Integration (CI) ✅ COMPLETE
*Goal: every push automatically builds and tests the app — no more "works on my machine."*

- [x] 3.1 Add a GitHub Actions workflow file (`.github/workflows/ci.yml`)
- [x] 3.2 Workflow: checkout → set up JDK → compile → run tests
- [x] 3.3 Push and confirm the Actions tab shows a green run
- [x] 3.4 Add a status badge to the README

**Commit point:** done via PRs, none directly to `main`.

---

## Phase 4 — Code Quality
*Goal: catch style/bug issues automatically, like real teams do with linting/static analysis.*

- [x] 4.1 Connect the repo to SonarCloud (free for public repos)
- [ ] 4.2 Fix flagged issues (code smells, duplicate code, etc.) — **DEFERRED**, revisit in Phase 10
- [x] 4.3 Add SonarCloud step into the CI workflow

---

## Phase 5 — Packaging & Release ✅ COMPLETE
*Goal: produce a real installable artifact, not just "run it in IntelliJ."*

- [x] 5.1 Use `jpackage` (built into the JDK) to produce a native app image
- [x] 5.2 Create a GitHub Release with the packaged app attached
- [x] 5.3 Tag the version (`v1.0.0`) following semantic versioning

**Note:** packaged via `jpackage --type app-image` (standalone folder + .exe,
bundled JVM, no installer). Gotcha: the SQLite driver needed `slf4j-api` at
runtime — added `slf4j-api-2.0.17.jar` and updated the jar manifest's `Class-Path`.
Released as `BankApp-v1.0.0-win.zip`, tagged `v1.0.0`.

---

## Phase 6 — (Stretch) Cloud Deployment ✅ COMPLETE
*Goal: experience real infrastructure — the part someone else usually owns.*

The desktop Swing app can't deploy to the cloud as-is, so the existing business
logic (`BankManager`, `Account` hierarchy — unchanged) sits behind a small
**Spring Boot REST API** in the `bank-api` module, and that is what is deployed.

- [x] 6.1 Wrap `BankManager` in a small Spring Boot app (`bank-api`, endpoint `GET /accounts`)
- [x] 6.2 Dockerize it (`Dockerfile` + `.dockerignore`, multi-stage build)
- [x] 6.3 Deploy to Render free tier (Docker web service, Frankfurt, `PORT=8080`)
- [x] 6.4 Managed Postgres (Render) instead of SQLite, selected via the `DATABASE_URL` environment variable; local runs still use SQLite
- [x] 6.5 Uptime monitoring with UptimeRobot (HTTP check on `/accounts`, every 5 min)

**Note:** free tier limits — the web service sleeps after inactivity, and the free
Postgres database expires on 2026-12-02 unless upgraded.

---

## Phase 7 — Bank-style Web UI (read-only) ✅ COMPLETE
*Goal: a real page people can open, served by the same Spring Boot service.*

- [x] 7.1 Jira epic "Web UI" (BA-27); static page `index.html` + `app.js` in `bank-api/src/main/resources/static/` fetches `GET /accounts` and shows account cards (BA-28)
- [x] 7.2 Bank look: header/navigation, total balance summary, account cards, responsive layout — `styles.css` with design tokens; SonarCloud floating-promise fix via ES module + top-level `await` (BA-29)
- [x] 7.3 Loading, empty and error states (for example the API waking from sleep) — skeleton cards + "Waking up" hint, empty message, error with Retry; tested with DevTools throttling, request blocking and local overrides (BA-30)

---

## Phase 8 — Authentication & Authorization ✅ COMPLETE
*Goal: only logged-in users see their own accounts.*

- [x] 8.1 Jira epic "Security" (BA-31); `users` and `account_owners` tables used only by `bank-api` (desktop app unchanged) — `DatabaseSettings`, `User`, `UserRepository` + 7 tests on a temp database; CI now also builds and tests `bank-api` (BA-32)
- [x] 8.2 Spring Security + BCrypt password hashing; register, login, logout (session cookie), CSRF protection — `SecurityConfig` (deny by default, `/accounts` open until 8.3), `AuthController` (`/api/register`, `/api/me`, `/api/csrf`), demo users `anna`/`max`/`admin` seeded only from `DEMO_PASSWORD`/`ADMIN_PASSWORD` env vars (BA-33)
- [x] 8.3 Protect the API: `/accounts` needs login and returns only the user's accounts; ADMIN sees all — `GET /accounts/{number}` returns 403 for other users' accounts; open `GET /api/health` (checks the database) replaces `/accounts` as the UptimeRobot target (BA-34)
- [x] 8.4 Login and Register pages in the UI; logout button — `login.html`, `register.html`, shared `api.js` (CSRF), username + Sign out in the header; done before 8.3 on purpose so the live site always has a working sign-in (BA-35)
- [x] 8.5 Security tests (401 without login, 403 for another user's account, no plain-text passwords) — `SecurityTest` (12 MockMvc tests incl. CSRF and BCrypt hash check); all `bank-api` tests now use a throwaway database via `TestDatabaseConfig` (the desktop tests in 10.2 still use `bank.db`) (BA-36)
- [x] 8.6 Secrets review: no credentials in code or repo — full history scanned (no leaks); session cookie HttpOnly + SameSite=Lax, Secure on Render via `SERVER_SERVLET_SESSION_COOKIE_SECURE=true`; `.gitignore` covers `*.db` and `.env` (BA-37)

---

## Phase 9 — Transactions in the UI ✅ COMPLETE
*Goal: deposit and withdraw from the browser, safely behind login.*

- [x] 9.1 Authenticated `POST` endpoints for deposit and withdraw (owner only), exceptions turned into clear messages — strict `BankManager.deposit`/`withdraw` (BA-40); `POST /accounts/{number}/deposit|withdraw` with 400/403/404/409 via `@ExceptionHandler`, 8 MockMvc tests (BA-41)
- [x] 9.2 UI forms and feedback messages — Deposit/Withdraw on each account card, inline amount form, green/red messages with the server's reason, balance and total update without reload (BA-42)
- [ ] 9.3 Optional: transaction history — **skipped for now**, kept in the backlog (BA-43)

---

## Phase 10 — Quality & Retro ✅ WRAPPED UP
*Goal: close the deferred work and reflect, like a real team at the end of a project.*

- [ ] 10.1 Revisit deferred SonarCloud findings (4.2) and add tests so new-code coverage passes; fix the 2 analysis warnings (set `sonar.java.binaries` and `sonar.sourceEncoding=UTF-8`) — **deferred**, backlog (BA-46)
- [ ] 10.2 Tests use an isolated test database — `bank-api` and the new core tests already do; `BankManagerTest` still uses `bank.db` — **deferred**, backlog (BA-47)
- [x] 10.3 Retro: what worked, what was painful, what I'd do differently — `RETRO.md` (BA-45)
- [x] 10.4 README: full architecture + how to run it (desktop, API, Docker, cloud) + live URL (BA-45)
- [ ] Also in the backlog: money as cents instead of `double` (BA-48); confirm the Secure cookie on Render (BA-38)

**Reminder:** the free Render Postgres expires on 2026-12-02 — decide to upgrade, recreate, or move host before then.

---

## Ground rules for the rest of this project
1. **No step is skipped** — if something isn't relevant, we mark it done/skipped explicitly, not silently drop it.
2. **You commit at every marked commit point** — I'll tell you exactly when and give you the commit message.
3. **Every ticket lives on the board** before we start coding it, not after.
4. **This file is the source of truth** — if the plan changes, we update this file, not just the chat.