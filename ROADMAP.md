# Bank App — Professional Software Engineering Roadmap

This is the master plan for turning the Bank app from a learning exercise into a
real, deployed, professionally-managed piece of software. Every phase below maps
to what actual software teams do. We follow this in order — nothing gets skipped.

**Team setup:** You play two roles — Developer and Product Owner/Scrum Master
(using a second account so the workflow feels real: someone assigns tickets,
someone picks them up).

**Budget:** Free tiers everywhere possible; a few dollars/month only where
strictly needed (e.g. a cheap cloud VM in Phase 6).

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

## Phase 1 — Backlog & Sprint Planning
*Goal: work is tracked like a real sprint, not ad-hoc chat requests.*

- [ ] 1.1 Write epics: `Testing`, `CI/CD`, `Code Quality`, `Packaging`, `Deployment`
- [ ] 1.2 Break epics into stories/tickets with acceptance criteria
- [ ] 1.3 Run a mini "Sprint Planning" — pick Sprint 1 scope (Testing epic)
- [ ] 1.4 Move tickets across the board as we do the work (To Do → In Progress → Done)

**Commit point:** none yet — this phase is planning only, no code changes.

---

## Phase 2 — Automated Testing
*Goal: prove the app works with tests, not just manual clicking.*

- [ ] 2.1 Add JUnit 5 as a dependency
- [ ] 2.2 Unit tests for `Account`, `SavingsAccount`, `CheckingAccount` (deposit/withdraw logic, exception thrown correctly)
- [ ] 2.3 Unit tests for `BankManager` (add/find/deposit/withdraw via the manager)
- [ ] 2.4 Run the full suite locally, confirm green

**Commit point:** after 2.4 — "Add JUnit test suite for model and service layers."

---

## Phase 3 — Continuous Integration (CI)
*Goal: every push automatically builds and tests the app — no more "works on my machine."*

- [ ] 3.1 Add a GitHub Actions workflow file (`.github/workflows/ci.yml`)
- [ ] 3.2 Workflow: checkout → set up JDK → compile → run tests
- [ ] 3.3 Push and confirm the Actions tab shows a green run
- [ ] 3.4 Add a status badge to the README

**Commit point:** after 3.2 — "Add GitHub Actions CI pipeline."

---

## Phase 4 — Code Quality
*Goal: catch style/bug issues automatically, like real teams do with linting/static analysis.*

- [ ] 4.1 Connect the repo to SonarCloud (free for public repos)
- [ ] 4.2 Fix flagged issues (code smells, duplicate code, etc.)
- [ ] 4.3 Add SonarCloud step into the CI workflow

**Commit point:** after fixes — "Address SonarCloud findings."

---

## Phase 5 — Packaging & Release
*Goal: produce a real installable artifact, not just "run it in IntelliJ."*

- [ ] 5.1 Use `jpackage` (built into the JDK) to produce a native app image
- [ ] 5.2 Create a GitHub Release with the packaged app attached
- [ ] 5.3 Tag the version (`v1.0.0`) following semantic versioning

**Commit point:** tag + release, not a code commit.

---

## Phase 6 — (Stretch) Cloud Deployment
*Goal: experience real infrastructure — the part someone else usually owns.*

This is the one phase that changes the app's shape a little: a desktop Swing app
can't "deploy to the cloud" as-is, so we'll expose the existing business logic
(`BankManager`, `Account` hierarchy — unchanged) behind a small **Spring Boot REST
API**, and deploy *that*. The Swing GUI stays as a local desktop client, OR we
build a minimal web front end — your call when we get there.

- [ ] 6.1 Wrap `BankManager` in a small Spring Boot app (new module, doesn't touch existing code)
- [ ] 6.2 Dockerize it (a `Dockerfile`)
- [ ] 6.3 Push image / deploy via Railway or Render free tier
- [ ] 6.4 Point a real (free) managed Postgres at it instead of local SQLite
- [ ] 6.5 Basic uptime monitoring (free tier, e.g. UptimeRobot)

**Commit point:** after each of 6.1–6.4 — separate commits, separate PRs if we want to simulate real review.

---

## Phase 7 — Retro
*Goal: do what real teams do at the end of a project — reflect.*

- [ ] 7.1 What worked, what was painful, what you'd do differently
- [ ] 7.2 Update the README with the full architecture + how to run it

---

## Ground rules for the rest of this project
1. **No step is skipped** — if something isn't relevant, we mark it done/skipped explicitly, not silently drop it.
2. **You commit at every marked commit point** — I'll tell you exactly when and give you the commit message.
3. **Every ticket lives on the board** before we start coding it, not after.
4. **This file is the source of truth** — if the plan changes, we update this file, not just the chat.