# Retrospective

A short look back at the project, like a team retro at the end of a project.
*(Draft — edit freely; this is my own reflection.)*

## What worked

- **Small tickets, one branch, one PR, one review.** Every change was small enough
  to test and understand, and `main` was never broken.
- **Protected `main` + CI.** The required `build-and-test` check caught problems
  before they reached the live site.
- **Deny by default.** Spring Security locks everything unless a rule opens it,
  so a forgotten URL is closed, not open.
- **Testing the unhappy paths on purpose.** DevTools throttling, request
  blocking and local overrides made loading, error and empty states visible.
- **Secrets only in environment variables.** A full-history scan found no leaks.
- **Tests on throwaway databases.** Tests never touch real data and can run anywhere.
- **Adding instead of changing shared code.** New strict deposit/withdraw methods
  left the desktop app untouched.

## What was painful

- **Copy-paste mistakes:** pasting into the wrong file, replacing a file that
  should only have been appended to, a truncated end of a file, a test file in
  `src/main` instead of `src/test`. Each was found by checks (`git status`,
  line counts, the Network tab) — but cost time.
- **Environment variables live per terminal tab.** The demo users were missing
  until the variables were set in the tab that runs the server.
- **Free hosting:** the server sleeps, and the free Postgres database expires.
- **Building without being able to compile right away** meant relying on the
  first real build on my PC and in CI.

## What I'd do differently

- **Money as cents or `BigDecimal`**, not `double`, from the start.
- **A build tool for the desktop code too** (Maven/Gradle) instead of `javac` + jars.
- **Database migrations** (e.g. Flyway) instead of `CREATE TABLE IF NOT EXISTS`.
- **Take the SonarCloud quality gate seriously from the beginning**, including
  test coverage, instead of deferring it.
- **Check after every paste** (`git diff --stat`, Ctrl+End) — make it a habit.

## Left in the backlog (recorded, not dropped)

- Transaction history (BA-43)
- SonarCloud cleanup and coverage (BA-46)
- Isolated test database for the desktop tests (BA-47)
- Money as cents instead of `double` (BA-48)
- Confirm the Secure session cookie on Render (BA-38)
- Decide about the free Postgres before **2026-12-02** (upgrade, recreate or move)
