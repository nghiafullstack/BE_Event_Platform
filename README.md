# Event Platform — microservices workspace

Monorepo Maven multi-module. Kế hoạch đầy đủ nằm ở doc "Event Platform — Kế hoạch Microservices + Flutter".

## Modules

| Module | Port | DB |
| --- | --- | --- |
| api-gateway | 8080 | — |
| identity-service | 8081 | identity_db |
| catalog-service | 8082 | catalog_db |
| event-service | 8083 | event_db |
| customer-service | 8084 | customer_db |
| notification-service | 8085 | — (RabbitMQ consumer) |
| shared-common | — | thư viện dùng chung: JWT filter, exception handler, BaseEntity, TenantContext |

## Chạy hạ tầng (Phase 0)

```bash
cp .env.example .env   # rồi điền giá trị thật
docker compose up -d
```

Kiểm tra: `docker compose ps` cả 3 container (mysql, redis, rabbitmq) phải `healthy`.

Cổng host: MySQL `3307`, Redis `6380`, RabbitMQ AMQP `5673`, RabbitMQ management UI
http://localhost:15673 — lệch so với cổng mặc định vì `6379`/`5672`/`15672` trên máy này đã bị một
project khác (Rencity) chiếm. Cổng bên trong container vẫn là mặc định (`6379`/`5672`), chỉ đổi cổng
map ra host.

## Build toàn bộ workspace

```bash
mvn -q -DskipTests package
```

## Chạy một service khi dev

Mỗi service đọc biến môi trường qua `spring.config.import=optional:file:.env[.properties]`
(tìm `.env` trong thư mục làm việc hiện tại). Cách đơn giản nhất khi chạy bằng `mvn spring-boot:run`
từ trong thư mục service: symlink hoặc copy `.env` ở root vào thư mục service, hoặc export biến môi
trường trong shell trước khi chạy:

```bash
set -a; source .env; set +a
mvn -pl identity-service spring-boot:run
```

## Phase 1 — identity-service

```bash
set -a; source .env; set +a
mvn -pl identity-service spring-boot:run
```

Lần chạy đầu tiên trên DB rỗng, `DataSeeder` tự tạo 3 role (`SUPER_ADMIN`, `ADMIN`, `TN_MEMBER`) và
1 tài khoản `SUPER_ADMIN` theo `SUPERADMIN_USERNAME`/`SUPERADMIN_PASSWORD` trong `.env`.

Test bằng curl/Postman:

```bash
# 1. Đăng ký tenant + admin đầu tiên (public)
curl -X POST http://localhost:8081/api/tenants/register -H "Content-Type: application/json" -d '{
  "name": "Doi Lan Su Rong ABC", "email": "abc@example.com", "domain": "abc",
  "adminUsername": "admin_abc", "adminPassword": "Admin@123"
}'

# 2. Login — trả về accessToken + refreshToken
curl -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin_abc","password":"Admin@123"}'

# 3. Gọi API có bảo vệ (Authorization: Bearer <accessToken>)
curl http://localhost:8081/api/auth/me -H "Authorization: Bearer <accessToken>"

# 4. RBAC — chỉ SUPER_ADMIN mới list được tất cả tenant (login bằng SUPERADMIN_USERNAME/PASSWORD ở .env)
curl http://localhost:8081/api/tenants -H "Authorization: Bearer <superadmin accessToken>"
# admin_abc gọi endpoint này sẽ nhận 403 Forbidden

# 5. Refresh access token
curl -X POST http://localhost:8081/api/auth/refresh -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

Phạm vi Phase 1 lược bớt so với BE_Event_Platform cũ để tự test được mà không cần notification-service
(chưa tới Phase 5): đăng ký tenant kích hoạt ngay (`isVerified=true`), không qua email xác thực/Redis
temp-password; admin đặt password trực tiếp lúc đăng ký. Auth trả JWT trong body (Bearer), không set
cookie như bản cũ — hợp với kiến trúc gateway tập trung ở Phase 6.

## Ghi chú bảo mật

- Không commit `.env`. `.env.example` chỉ chứa placeholder.
- Mật khẩu SMTP Gmail dùng ở `SMTP_USERNAME`/`SMTP_PASSWORD` phải là App Password **mới**, khác với
  cái đã lộ plaintext trong `BE_Event_Platform/src/main/resources/application.properties` (đã bị
  commit lên git) — cái cũ coi như đã lộ, phải revoke trong Google Account trước khi tạo cái thay thế.
