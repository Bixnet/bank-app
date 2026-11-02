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
- [ ] 4.2 Fix flagged issues (code smells, duplicate code, etc.) — **DEFERRED**, to revisit after Phase 7
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