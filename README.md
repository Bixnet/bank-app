# Bank App

[![CI](https://github.com/Bixnet/bank-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Bixnet/bank-app/actions/workflows/ci.yml)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F)
![SQLite](https://img.shields.io/badge/SQLite-persistence-003B57)

A small banking system in Java with a **Swing desktop client** and a **Spring Boot REST API** sharing one domain core. Accounts persist to SQLite, every change is unit-tested, and CI runs on each push.

It started as an OOP exercise and is being grown into a production-style project step by step — see [ROADMAP.md](ROADMAP.md).

## Features

- **Two account types** — `SavingsAccount` (interest) and `CheckingAccount` (overdraft limit) built on an abstract `Account`
- **Domain rules enforced in the model** — withdrawals beyond balance/overdraft raise `InsufficientFundsException`
- **SQLite persistence** via JDBC prepared statements (`AccountRepository`)
- **Desktop GUI** — deposit, withdraw and refresh from a FlatLaf-styled Swing table
- **REST API** — `GET /accounts` returns all accounts as JSON
- **CI** — compile, JUnit 5 tests and SonarQube analysis on GitHub Actions

## Architecture

```
src/bank/
├── model/       Account, SavingsAccount, CheckingAccount, BankOperations
├── service/     BankManager — business operations + logging
├── db/          AccountRepository — SQLite via JDBC
├── gui/         BankFrame — Swing client
└── exceptions/  InsufficientFundsException
bank-api/        Spring Boot app reusing the same model + service
test/            JUnit 5 tests for model and service layers
```

The GUI and the API are thin shells over the same `BankManager`, so business logic lives in one place.

## Getting started

Requires **JDK 21**.

**Desktop app**

```bash
mkdir -p out
javac -cp "libs/*" -d out $(find src -name "*.java")
java -cp "libs/*:out" bank.Main
```

**Tests**

```bash
javac -cp "libs/*:out" -d out $(find test -name "*.java")
java -cp "libs/*:out" org.junit.platform.console.ConsoleLauncher --scan-classpath=out
```

**REST API**

```bash
cd bank-api
./mvnw spring-boot:run
curl http://localhost:8080/accounts
```

## Next steps

- Store money as `BigDecimal` (or integer cents) instead of `double`
- Replace the `libs/` jars with Maven-managed dependencies for the desktop app
- Add `POST` endpoints for deposit / withdraw with validation
- Package a runnable release (Phase 5 of the roadmap)

## License

[MIT](LICENSE)
