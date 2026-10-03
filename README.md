# CaseStudy — Abandoned Cart Recovery Pipeline

Spring Boot 3 (Java 21) multi-module app with a **detect → schedule → dispatch → send (mock)** pipeline.

## Modules

| Module | Role |
| --- | --- |
| `casestudy-app` | HTTP API, pipeline, pluggable persistence |
| `ConfigService` | Default reminder windows, template, enabled flag |
| `AbService` | 5-bucket experiments (windows, template, `reminderEnabled`) |

## Pipeline

1. **Detect** — `AbandonmentDetector` (idempotent on `eventId`, version bump, cancel pending)
2. **Schedule** — `NotificationScheduler` (AB + config → `PENDING` jobs)
3. **Dispatch** — `ReminderJobRunner` claims due rows (`SKIP LOCKED` on MySQL)
4. **Send** — `NotificationDispatcher` + **Strategy** channels (`Email` / `Sms` / `Push` mocks). Status becomes **`FIRED` only after mock publish succeeds**.

## Storage (`casestudy.storage`)

| Value | Description |
| --- | --- |
| `in-memory` | Default profile `local-inmemory`; no MySQL required |
| `mysql` | JPA + MySQL (`spring.profiles.active=mysql`) |
| `mysql-redis` | MySQL + `DueReminderIndex` decorator (in-process Redis simulation; swap for `RedisTemplate`) |

## Run (in-memory, default)

```powershell
cd casestudy-app
mvn spring-boot:run
```

## Run (MySQL)

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
mvn spring-boot:run -pl casestudy-app
```

## APIs

**Ingest (detect + schedule)**

```powershell
curl -X POST http://localhost:8080/api/cart/events `
  -H "Content-Type: application/json" `
  -d '{"eventId":"evt-1","cartId":"cart-1","userId":"user-1","userType":"LOGGED_IN","activityType":"EDIT","activityTime":"2026-10-02T18:00:00"}'
```

**Dispatch due jobs**

```powershell
curl -X POST "http://localhost:8080/api/reminders/dispatch?limit=100"
```

## Tests (fake clock)

```powershell
mvn test -pl casestudy-app
```

`FakeClockPipelineVerifierTest` advances `MutableClock`, runs dispatch, and asserts jobs reach **`FIRED`** after mock publish.

## Package layout (casestudy-app)

```
com.casestudy.dao.{memory,mysql,redis}   # CartActivityStore, ReminderScheduleStore
com.casestudy.pipeline.detect                    # AbandonmentDetector
com.casestudy.pipeline.schedule                  # NotificationScheduler
com.casestudy.pipeline.dispatch                  # ReminderJobRunner, FireTimeGuard
com.casestudy.pipeline.send                      # NotificationChannel strategies, dispatcher
```
