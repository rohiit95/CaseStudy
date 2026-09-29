# CaseStudy

Runnable Spring Boot 3 app (Java 21, Maven) that connects to a **local MySQL** database.

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL running on `localhost:3306`

Default credentials are `root` with an empty password. Override with environment variables if your local MySQL differs.

## Database

On first start, the JDBC URL uses `createDatabaseIfNotExist=true`, so the `casestudy` schema is created if it is missing. You can also create it yourself:

```sql
CREATE DATABASE IF NOT EXISTS casestudy;
```

## Run

From this directory (Maven Wrapper is included; a global Maven install is not required):

```powershell
# Optional: set if your MySQL user/password is not root / empty
$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = "your-password"

.\mvnw.cmd spring-boot:run
```

The API listens on [http://localhost:8080](http://localhost:8080).

## Cart and reminder APIs

Create a cart activity row and internally insert `ReminderSchedule` rows (defaults: 30, 60, and 1440 minutes from `lastActivityTime`). If the `cartId` already exists, pending reminders are cancelled, `activityVersion` is incremented, and a new set of reminders is scheduled.

```powershell
curl -X POST http://localhost:8080/api/carts `
  -H "Content-Type: application/json" `
  -d '{"cartId":"cart-100","userId":"user-9"}'
```

Poll due reminders using:

`SELECT * FROM ReminderSchedule WHERE status = 'PENDING' AND scheduledAt <= NOW() LIMIT 100 FOR UPDATE SKIP LOCKED`

The API claims those rows (`status = CLAIMED`) so concurrent workers do not pick the same jobs:

```powershell
curl -X POST "http://localhost:8080/api/reminders/claim?limit=100"
```

To exercise the poll immediately, create a cart with a 0-minute window:

```powershell
curl -X POST http://localhost:8080/api/carts `
  -H "Content-Type: application/json" `
  -d '{"cartId":"cart-due","reminderWindowsInMins":[0]}'
curl -X POST "http://localhost:8080/api/reminders/claim?limit=100"
```

## Verify

1. Actuator health (includes the DB check):

   ```powershell
   curl http://localhost:8080/actuator/health
   ```

   You should see `"status":"UP"` and a `db` component.

2. Direct MySQL ping:

   ```powershell
   curl http://localhost:8080/api/status
   ```

   You should see `"database":"connected"` plus the MySQL product name and catalog `casestudy`.

## Configuration

| Variable | Default |
| --- | --- |
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/casestudy?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `MYSQL_USER` | `root` |
| `MYSQL_PASSWORD` | (empty) |
| `SERVER_PORT` | `8080` |

## Tests

Unit/context tests use an in-memory H2 database (`application-test.yml`) so they do not need MySQL:

```powershell
.\mvnw.cmd test
```
