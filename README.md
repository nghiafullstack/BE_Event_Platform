# Event Platform — microservices workspace

Monorepo Maven multi-module. Kế hoạch đầy đủ nằm ở doc "Event Platform — Kế hoạch Microservices + Flutter".

## Modules

Cấu trúc theo convention của rencity-platform-spring: service ở `services/`, thư viện dùng chung ở `libs/`.

| Module | Port | DB |
| --- | --- | --- |
| services/api-gateway | 8080 | — |
| services/identity-service | 8081 | identity_db |
| services/catalog-service | 8082 | catalog_db |
| services/event-service | 8083 | event_db |
| services/customer-service | 8084 | customer_db |
| services/notification-service | 8085 | — (RabbitMQ consumer) |
| libs/shared-common | — | JWT filter, exception handler, BaseEntity, TenantContext |

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
mvn -pl services/identity-service spring-boot:run
```

## Phase 1 — identity-service

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

## Phase 2 — event-service

```bash
set -a; source .env; set +a
mvn -pl services/identity-service spring-boot:run &   # cần identity-service để lấy JWT
mvn -pl services/event-service spring-boot:run &
```

Test full flow bằng curl/Postman (cần 1 tenant + admin đã đăng ký ở Phase 1 trước):

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin_abc","password":"Admin@123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")

# 1. Tenant admin tự tạo show cho đội mình (platformFee=0, tenantId lấy từ JWT)
curl -X POST http://localhost:8083/api/events -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{
  "name": "Khai truong ABC", "type": "GRAND_OPENING", "eventDate": "2026-09-20",
  "location": "123 Nguyen Trai", "customerId": 1, "totalAmount": 5000000
}'
# -> lấy "id" làm EVENT_ID

# 2. Gán chính admin (userId trong response login) làm thành viên đi diễn
curl -X POST http://localhost:8083/api/tenant/events/<EVENT_ID>/assign -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" -d '[{"userId":<USER_ID>,"position":"DAU_LAN"}]'
# -> event tự chuyển CONFIRMED, lấy "id" trong response with-members làm USER_EVENT_ID

# 3. Member phản hồi, check-in tập trung, check-in điểm diễn, check-out
curl -X PATCH "http://localhost:8083/api/tenant/events/assignments/<USER_EVENT_ID>/respond?status=ACCEPTED" -H "Authorization: Bearer $TOKEN"
curl -X POST "http://localhost:8083/api/tenant/events/assignments/<USER_EVENT_ID>/concentrate-check-in" -H "Authorization: Bearer $TOKEN"
curl -X POST "http://localhost:8083/api/tenant/events/assignments/<USER_EVENT_ID>/check-in?location=10.76,106.66" -H "Authorization: Bearer $TOKEN"
curl -X POST "http://localhost:8083/api/tenant/events/assignments/<USER_EVENT_ID>/check-out" -H "Authorization: Bearer $TOKEN"
# -> show tự động chuyển IN_PROGRESS rồi COMPLETED khi người cuối cùng checkout

# 4. Dashboard / summary
curl "http://localhost:8083/api/tenant/events/summary?month=9&year=2026" -H "Authorization: Bearer $TOKEN"
curl "http://localhost:8083/api/tenant/events/dashboard-member/<USER_ID>" -H "Authorization: Bearer $TOKEN"

# 5. Sàn đẩy show thẳng cho 1 tenant cụ thể (không cần token, tự tính platformFee 10%)
curl -X POST http://localhost:8083/api/events -H "Content-Type: application/json" -d '{
  "name": "Tiec cuoi", "type": "WEDDING", "eventDate": "2026-09-25",
  "location": "456 Le Loi", "customerId": 2, "tenantId": <TENANT_ID>, "totalAmount": 8000000
}'
# -> tenant admin accept/reject bằng POST /api/tenant/events/<id>/accept|reject
```

Kiểm tra RabbitMQ nhận message (assign/respond/auto-complete đều publish):
`docker exec event-platform-rabbitmq rabbitmqctl list_queues name messages` → `notification.queue` phải tăng.

Đơn giản hoá có chủ đích so với bản cũ:
- `Event.customerId`/`tenantId` và `UserEvent.userId` là ID thuần (Long), không còn JPA `@ManyToOne` sang
  Customer/Tenant/User — đúng ranh giới database-per-service. Response chỉ trả ID, chưa join tên hiển thị
  (customerName, tenantName, fullName của member) vì customer-service (Phase 3) chưa có API thật và
  identity-service chưa expose "get user by id" — việc enrich tên qua REST để dành làm sau, không chặn DoD.
- Không còn gọi FCM/email trực tiếp — mọi thông báo (gán thành viên, member phản hồi, show tự hoàn thành,
  đổi giờ tập trung) publish vào RabbitMQ queue `notification.queue` (contract `NotificationMessage` ở
  shared-common); notification-service (Phase 5) sẽ là consumer thật.
- Bỏ EventSpecification lọc động (search/status/type) của bản cũ, chỉ giữ list theo tenant + theo tháng —
  đủ cho DoD, có thể thêm lại filter sau nếu cần.
- **Fix 1 bug kế thừa từ bản cũ**: `countFinishedShows`/`sumTotalEarnings` lọc theo status `CHECKED_OUT`
  nhưng `checkOut()` thực ra set status `COMPLETED` — status `CHECKED_OUT` chưa bao giờ được gán ở bất kỳ
  bước nào nên dashboard cũ luôn ra 0 show đã hoàn thành. Đã sửa lọc theo `COMPLETED` cho đúng.
- **Fix 1 lỗ hổng quyền kế thừa từ bản cũ**: các API respond/check-in/check-out/concentrate-check-in trong
  bản cũ không kiểm tra ai đang gọi — bất kỳ user đăng nhập nào cũng check-in/checkout hộ được userEventId
  bất kỳ. Bản mới bắt buộc `userId` trong JWT phải khớp chủ của bản ghi phân công (trừ khi người gọi có
  role ADMIN/SUPER_ADMIN).

## Phase 3 — customer-service

```bash
set -a; source .env; set +a
mvn -pl services/identity-service spring-boot:run &
mvn -pl services/customer-service spring-boot:run &
mvn -pl services/event-service spring-boot:run &      # để test gán khách vào show
```

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin_abc","password":"Admin@123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")

# CRUD khách hàng — mọi endpoint scope theo tenantId lấy từ JWT
curl -X POST http://localhost:8084/api/customers -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"fullName":"Nguyen Van A","phone":"0901234567","type":"INDIVIDUAL"}'
# -> lấy "id" làm CUSTOMER_ID

curl "http://localhost:8084/api/customers?keyword=nguyen" -H "Authorization: Bearer $TOKEN"
curl http://localhost:8084/api/customers/<CUSTOMER_ID> -H "Authorization: Bearer $TOKEN"
curl -X PUT http://localhost:8084/api/customers/<CUSTOMER_ID> -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"fullName":"Nguyen Van A VIP","phone":"0901234567","type":"BUSINESS"}'
curl -X DELETE http://localhost:8084/api/customers/<CUSTOMER_ID> -H "Authorization: Bearer $TOKEN"

# Gán khách hàng vào show khi tạo event (event-service)
curl -X POST http://localhost:8083/api/events -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{
  "name": "Khai truong", "type": "GRAND_OPENING", "eventDate": "2026-10-01",
  "location": "999 Hung Vuong", "customerId": <CUSTOMER_ID>, "totalAmount": 3000000
}'
```

Đơn giản hoá: `phone` chỉ unique theo `(tenant_id, phone)` — bản cũ khai `unique=true` toàn cục trên
cột `phone` trong khi service lại chỉ check trùng theo tenant, tự mâu thuẫn. `assignedTo`/`tenant` là
`Long` thuần thay vì JPA relation sang identity-service. event-service chỉ lưu `customerId` khi tạo
show, chưa gọi REST sang customer-service để validate/enrich tên — để dành cho sau, không chặn DoD.

**Fix quan trọng áp dụng cho mọi service (identity/event/customer) trong lúc làm Phase 3**: phát hiện
`server.error.include-message=always` không có tác dụng trên bản Spring Boot đang dùng (đã thử cả
override qua command-line, vẫn không thấy field `message`) — mọi lỗi nghiệp vụ (RuntimeException) trả về
403 rỗng hoặc 500 không rõ nguyên nhân, không tự test được. Đã thêm hẳn `@ExceptionHandler` cho
`EntityNotFoundException` (404), `IllegalStateException` (409), `RuntimeException` (400) vào
`GlobalExceptionHandler` ở `shared-common` — giờ mọi lỗi đều trả JSON `{status, error, message}` rõ ràng.
Đồng thời phát hiện thêm: thiếu `permitAll` cho path `/error` khiến Spring Security tự chặn luôn request
forward nội bộ khi có exception (403 rỗng che mất lỗi thật) — đã thêm vào cả 3 `SecurityConfig`.

## Ghi chú bảo mật

- Không commit `.env`. `.env.example` chỉ chứa placeholder.
- Mật khẩu SMTP Gmail dùng ở `SMTP_USERNAME`/`SMTP_PASSWORD` phải là App Password **mới**, khác với
  cái đã lộ plaintext trong `BE_Event_Platform/src/main/resources/application.properties` (đã bị
  commit lên git) — cái cũ coi như đã lộ, phải revoke trong Google Account trước khi tạo cái thay thế.
