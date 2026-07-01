# Heavy Equipment Rental & Booking System — Backend

Spring Boot REST API backend.

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.x running locally (or update `application-dev.yml`)

## Setup

1. Create MySQL database (auto-created if `createDatabaseIfNotExist=true`, but make sure MySQL is running):
   ```sql
   CREATE DATABASE heavy_equipment_rental;
   ```

2. Set environment variables (or edit `application-dev.yml` directly for local dev):
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=yourpassword
   export JWT_SECRET=YourLongRandomSecretKeyHere
   ```

3. Run the app:
   ```bash
   mvn spring-boot:run
   ```

4. App starts on: `http://localhost:8080`

## Profiles
- `dev` (default) — local MySQL, `ddl-auto: update`, SQL logging on.
- `prod` — uses env vars for DB connection, `ddl-auto: validate` (no auto schema changes).

Activate prod profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Project Structure
See `/src/main/java/com/heavyequip/rental/` — organized by layer:
`config`, `controller`, `dto`, `entity`, `repository`, `service`, `security`, `exception`, `mapper`, `util`.

## Next Steps (in this build)
1. Entity classes (User, Equipment, Booking) — **next**
2. Repositories
3. DTOs
4. Service layer (booking conflict logic, price calculation)
5. Spring Security + JWT
6. Controllers
