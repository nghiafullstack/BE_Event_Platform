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
| services/notification-service | 8085 | notification_db (inbox + fcm_tokens) |
| libs/shared-common | — | JWT filter, exception handler, BaseEntity, TenantContext |

## Quy ước API chung (áp dụng mọi service, kể cả qua gateway)

**1. Mọi request/response body đều `snake_case`** — cấu hình 1 dòng
`spring.jackson.property-naming-strategy=SNAKE_CASE` ở từng service (Jackson tự áp dụng cho cả chiều
đọc lẫn ghi, field Java vẫn viết camelCase bình thường, không phải đổi tên field trong code). Ví dụ field
Java `fullName`, `accessToken`, `tenantId` sẽ là `full_name`, `access_token`, `tenant_id` trên JSON.

**2. Mọi response đều bọc trong 1 envelope chung** — `ResponseWrappingAdvice` (`shared-common`) tự động
bọc mọi response của mọi controller, không cần sửa từng controller:

```json
// thanh cong
{ "success": true, "code": "OK", "message": null, "data": { ... hoac list/page ... } }

// loi
{ "success": false, "code": "CUSTOMER_PHONE_DUPLICATE", "message": "Số điện thoại này đã tồn tại...", "data": null }
```

204 No Content (DELETE) vẫn giữ nguyên không có body, không bị bọc.

**3. Mã lỗi (`code`) ổn định, tách khỏi message tiếng Việt** — dùng khi client cần phân biệt case cụ
thể mà không parse chuỗi. `ApiException(HttpStatus, code, message)` ở `shared-common`, mỗi service tự định
nghĩa mã lỗi riêng của mình (không dùng 1 enum chung cho cả hệ thống, tránh phải sửa file chung mỗi khi
thêm lỗi mới), quy ước `DOMAIN_LY_DO` viết hoa, ví dụ `CUSTOMER_PHONE_DUPLICATE`, `EVENT_ASSIGNMENT_NOT_OWNED`.
Cơ chế mới được thêm, **áp dụng dần** — code cũ (`RuntimeException`/`IllegalStateException`/
`EntityNotFoundException` chung chung) vẫn chạy được, tự động nhận `code` mặc định (`BAD_REQUEST`,
`CONFLICT`, `NOT_FOUND`) qua `GlobalExceptionHandler`, chỉ nơi nào cần code cụ thể mới đổi sang `throw new
ApiException(...)`.

> Lưu ý: các ví dụ `curl` ở từng Phase bên dưới được viết **trước** khi có 2 quy ước trên — field trong
> JSON mẫu vẫn ghi camelCase (`adminUsername`, `customerId`...) và response mẫu không có field `success/
> code/data` bọc ngoài. Endpoint/logic vẫn đúng y nguyên, chỉ khác đúng 2 chỗ: gửi field bằng snake_case,
> và đọc kết quả thật từ `response.data` thay vì đọc thẳng.

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
  Customer/Tenant/User — đúng ranh giới database-per-service. Response đã enrich tên hiển thị qua REST
  nội bộ (`customer_name`, `tenant_name`, `user_full_name`, `vendor_business_name`,
  `service_category_name`) — xem mục Internal API.
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

## Phase 4 — catalog-service

```bash
set -a; source .env; set +a
mvn -pl services/identity-service spring-boot:run &
mvn -pl services/catalog-service spring-boot:run &
```

```bash
SUPER_TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d "{\"username\":\"superadmin\",\"password\":\"$SUPERADMIN_PASSWORD\"}" | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")

# 1. SUPER_ADMIN tạo tối thiểu 2 loại vendor (public GET, chỉ SUPER_ADMIN mới tạo/sửa/xoá được)
curl -X POST http://localhost:8082/api/service-categories -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPER_TOKEN" -d '{"name":"Lan Su Rong","code":"lan-su-rong"}'
curl -X POST http://localhost:8082/api/service-categories -H "Content-Type: application/json" \
  -H "Authorization: Bearer $SUPER_TOKEN" -d '{"name":"Ban Nhac","code":"ban-nhac"}'
curl http://localhost:8082/api/service-categories   # public, không cần token

# 2. Mỗi tenant tự tạo/sửa hồ sơ vendor của mình (upsert), tham chiếu 1 serviceCategoryId
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin_abc","password":"Admin@123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")
curl -X PUT http://localhost:8082/api/tenant/vendor-profile -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{
  "serviceCategoryId": 1, "businessName": "Doan Lan ABC", "address": "Q5, TPHCM"
}'

# 3. Hồ sơ vendor công khai — ai cũng xem được, lọc theo loại
curl "http://localhost:8082/api/vendor-profiles"
curl "http://localhost:8082/api/vendor-profiles?serviceCategoryId=1"
curl "http://localhost:8082/api/vendor-profiles/<id>"
```

Thiết kế "Tenant tham chiếu tới ServiceCategory" theo đúng ranh giới database-per-service: **không** thêm
cột `serviceCategoryId` vào `Tenant` ở identity-service (sẽ tạo phụ thuộc ngược service). Thay vào đó
`VendorProfile` (ở catalog-service) giữ cả `tenantId` lẫn `serviceCategoryId` — đây chính là chỗ tenant
"tham chiếu" tới category, 1 tenant = 1 vendor profile = 1 category cho MVP. Đã verify DoD: tạo 2 tenant,
mỗi tenant chọn 1 category khác nhau (Lân Sư Rồng / Ban Nhạc), list public lọc đúng theo từng loại.

## Phase 5 — notification-service

```bash
set -a; source .env; set +a
mvn -pl services/identity-service spring-boot:run &
mvn -pl services/event-service spring-boot:run &
mvn -pl services/notification-service spring-boot:run &
```

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin_abc","password":"Admin@123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")

# 1. Đăng ký FCM token cho chính mình (userId lấy từ JWT, không tin body như bản cũ)
curl -X POST http://localhost:8085/api/fcm/register -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" -d '{"token":"device-token-xyz"}'

# 2. Tạo show + gán chính mình làm thành viên -> publish MEMBER_ASSIGNED -> xem log notification-service
curl -X POST http://localhost:8083/api/events -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{...}'
curl -X POST http://localhost:8083/api/tenant/events/<EVENT_ID>/assign -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" -d '[{"userId":<USER_ID>,"position":"DAU_LAN"}]'
# -> log: "[DEV] would send FCM push to token=device-token-xyz title=..."

# 3. Từ chối show -> publish MEMBER_REJECTED (broadcast tới admin của tenant) -> vừa push vừa email
curl -X PATCH "http://localhost:8083/api/tenant/events/assignments/<USER_EVENT_ID>/respond?status=REJECTED&note=..." \
  -H "Authorization: Bearer $TOKEN"
```

**Vì sao push chỉ log "[DEV] would send" chứ không bắn thật**: `FCM_CREDENTIALS_JSON`/`SMTP_PASSWORD` trong
`.env` vẫn là placeholder (chưa có project Firebase thật, và mật khẩu Gmail thật vẫn chờ ông tự rotate —
xem ghi chú bảo mật). `FirebaseConfig` phát hiện placeholder và tự chuyển `FcmPushService` sang chế độ log
thay vì gọi Firebase thật, nên pipeline vẫn test được đầy đủ (đúng recipient, đúng nội dung) mà không cần hạ
tầng thật; khi có Firebase project + có app Flutter (Phase 7) sinh token thật, chỉ cần điền
`FCM_CREDENTIALS_JSON` là chuyển sang gửi push thật ngay, không phải sửa code. Email cũng vậy: gọi SMTP
thật, bắt lỗi gọn (không crash consumer) — verify bằng cách thấy đúng log `AuthenticationFailedException`
tới đúng địa chỉ email của admin thay vì bay lỗi ra ngoài làm chết listener.

**Kiến trúc đáng chú ý:**
- notification-service có `notification_db`: bảng `notifications` (inbox cho chuông UI) và `fcm_tokens`
  (device token). Consumer: persist inbox trước → rồi mới bắn FCM. Idempotent theo
  `(message_id, user_id)`.
- Dọn FCM token: (1) khi Firebase trả `UNREGISTERED`/`INVALID_ARGUMENT` → xóa token ngay;
  (2) job 03:30 mỗi ngày xóa token không `last_seen_at` trong `FCM_TOKEN_RETENTION_DAYS` (mặc định 90).
  App gọi `DELETE /api/fcm/register` khi logout để gỡ token chủ động.
- Inbox API (qua gateway): `GET /api/notifications`, `GET /api/notifications/unread-count`,
  `POST /api/notifications/{id}/read`, `POST /api/notifications/read-all`.
- Message kiểu broadcast (`recipientUserId=null`, chỉ có `tenantId`) cần biết ai là admin của tenant đó —
  endpoint nội bộ `GET /api/internal/tenants/{tenantId}/admins` ở identity-service, bảo vệ bằng header
  `X-Internal-Token` (= `INTERNAL_SERVICE_TOKEN`) — gateway **không** route `/api/internal/**`. Xem mục
  "Internal API (service-to-service)" bên dưới.

## Phase 6 — api-gateway (điểm vào duy nhất)

### Chạy rời từng service (dev, không qua Docker)

```bash
set -a; source .env; set +a
for s in identity-service catalog-service event-service customer-service notification-service api-gateway; do
  mvn -pl services/$s spring-boot:run &
done
```

Sau đó gọi mọi API qua `http://localhost:8080` thay vì port riêng của từng service — gateway tự route
theo path và tự kiểm tra JWT (401 ngay tại gateway nếu thiếu/sai token, trước khi chạm tới service phía
sau):

```bash
curl -X POST http://localhost:8080/api/tenants/register -H "Content-Type: application/json" -d '{...}'
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login ... | ...)
curl http://localhost:8080/api/customers -H "Authorization: Bearer $TOKEN"          # -> customer-service
curl http://localhost:8080/api/tenant/vendor-profile -H "Authorization: Bearer $TOKEN"  # -> catalog-service
curl http://localhost:8080/api/tenant/events -H "Authorization: Bearer $TOKEN"      # -> event-service
```

### Chạy toàn bộ hệ thống qua Docker Compose (DoD Phase 6)

```bash
mvn -q -DskipTests package        # build jar cho cả 6 service trước
docker compose build              # build image cho 6 service (infra dùng image có sẵn)
docker compose up -d              # lên toàn bộ: 3 infra + 6 app service
docker compose ps                 # tất cả phải Up/healthy
```

Chỉ `api-gateway` publish port ra host (`8080:8080`) — 5 service còn lại **không** có `ports:` trong
compose, chỉ gọi được với nhau qua tên service trong mạng Docker nội bộ (`http://identity-service:8081`,
v.v., cấu hình qua biến `x-app-env` dùng chung trong `docker-compose.yml`). Đã verify: `curl localhost:8081`
(và 8082-8085) không kết nối được từ host, chỉ `localhost:8080` phản hồi — đúng nghĩa "gọi API qua 1 cổng
duy nhất". Toàn bộ flow register → login → tạo khách hàng → tạo show → gán thành viên đã chạy end-to-end
qua gateway trên hệ thống Docker thật, kể cả message RabbitMQ tới notification-service.

Dockerfile của mỗi service chỉ COPY jar đã build sẵn (không build Maven bên trong Docker) — đơn giản hoá
có chủ đích cho vòng lặp dev nhanh; multi-stage build tái lập được từ source (không cần host có Maven) để
dành cho phase "Integration + Deploy FPT Cloud" khi cần pipeline CI/CD thật.

### Kiến trúc: vì sao không dùng Spring Cloud Gateway

Ban đầu dùng `spring-cloud-starter-gateway` (Spring Cloud 2025.0.0, bản duy nhất tương thích Spring Boot
4) đúng như plan gợi ý ("Spring Cloud Gateway hoặc Nginx"), nhưng gặp hàng loạt lỗi khởi động do các class
autoconfiguration của spring-cloud-commons/gateway-server còn tham chiếu path autoconfigure cũ của Spring
Boot 3.x (`org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration`,
`...orm.jpa.HibernateJpaAutoConfiguration`, `...web.embedded.NettyWebServerFactoryCustomizer`...) — Boot 4
đã tách các package này ra module riêng (`spring-boot-jdbc`, `spring-boot-hibernate`...). Sau khi loại trừ
được vài lớp không quan trọng (`LifecycleMvcEndpointAutoConfiguration`, `RefreshAutoConfiguration`,
`SimpleDiscoveryClientAutoConfiguration`...) thì gặp phải `GatewayAutoConfiguration$NettyConfiguration` —
chính lớp dựng Netty server lõi của gateway — nghĩa là bản Spring Cloud Gateway 4.3.0 này chưa thực sự
tương thích Boot 4 ở phần cốt lõi, không chỉ tính năng phụ.

Quyết định: bỏ Spring Cloud Gateway, tự viết reverse-proxy bằng WebFlux thuần (`GatewayProxyFilter`, một
`WebFilter` duy nhất) — route theo bảng path tĩnh (`RouteTable`) + forward bằng `WebClient`, tái dùng thẳng
`JwtTokenProvider` từ `shared-common` để check JWT. Nhẹ hơn, không phụ thuộc thêm hệ sinh thái Spring Cloud
(vốn cũng không cần thiết cho quy mô 6 service cố định, không cần service discovery/load balancing động),
và tránh được toàn bộ lớp tương thích nói trên. Đây vẫn nằm trong phạm vi plan cho phép ("Spring Cloud
Gateway **hoặc** Nginx") — chỉ là lựa chọn thứ 3 tự triển khai thay vì dùng nguyên khối có sẵn.

**Tóm lại: không dùng Spring Cloud (Gateway / Eureka / OpenFeign) vì** (1) Spring Cloud Gateway 4.3 chưa
chạy ổn với Spring Boot 4.0.x trong workspace này; (2) topology cố định 6 service + Docker DNS theo tên
service, không cần discovery/LB động; (3) call chéo dùng `RestClient` + `/api/internal/**` +
`INTERNAL_SERVICE_TOKEN` là đủ.

## Internal API (service-to-service)

Các cạnh call chéo trong plan (`event → identity/catalog/customer`, `notification → identity`) đi qua
REST nội bộ, **không** qua gateway:

| Caller | Callee | Endpoint |
| --- | --- | --- |
| notification-service | identity-service | `GET /api/internal/tenants/{id}/admins` |
| event-service | identity-service | `GET /api/internal/tenants/{id}`, `GET /api/internal/users/{id}`, `GET /api/internal/users?ids=` |
| event-service | customer-service | `GET /api/internal/customers/{id}` |
| event-service | catalog-service | `GET /api/internal/vendor-profiles/by-tenant/{tenantId}` |

Quy ước chung:
- Header bắt buộc: `X-Internal-Token: <INTERNAL_SERVICE_TOKEN>` (cùng giá trị trên mọi service).
- `InternalServiceAuthFilter` (shared-common) chặn `/api/internal/**` nếu thiếu/sai token.
- Response nội bộ **không** bọc `ApiResponse` (để `RestClient` bind thẳng DTO).
- Gateway `RouteTable` cố ý **không** có pattern `/api/internal/**`.
- Base URL cấu hình qua `IDENTITY_SERVICE_URL` / `CATALOG_SERVICE_URL` / `CUSTOMER_SERVICE_URL`
  (Docker: `http://identity-service:8081`, …).

Khi tạo show, event-service **validate** `customerId` tồn tại và thuộc đúng tenant; khi gán thành viên,
validate user thuộc tenant. Khi đọc show/assignment, enrich tên hiển thị (best-effort: lỗi gọi S2S
không làm sập response đọc, chỉ để trống tên).

Tối ưu N+1: `EventService.getTenantEvents`/`getTenantSchedule` (list phân trang) chỉ gọi
`identityServiceClient.findTenant` + `catalogServiceClient.findVendorByTenant` **1 lần cho cả trang**
(qua `TenantVendorContext`, vì mọi show trong 1 trang cùng thuộc 1 tenant) thay vì gọi lại cho từng dòng.
`customerServiceClient.findCustomer` vẫn gọi theo từng dòng (mỗi show có thể khác khách hàng) — chấp nhận
được ở quy mô hiện tại, có thể thêm endpoint lookup hàng loạt ở customer-service sau nếu cần.

### 2 bug đã tìm và sửa khi verify end-to-end phần internal API này

1. **`InternalRestClients` deserialize sai do lệch naming strategy — enrich luôn ra `null` mà không log
   lỗi nào.** `spring.jackson.property-naming-strategy=SNAKE_CASE` chỉ áp cho `ObjectMapper` do Boot tự
   cấu hình cho tầng MVC (server request/response); `RestClient.builder().build()` gọi trực tiếp (không
   qua Spring context) lại dùng converter Jackson mặc định (camelCase). Field 1 từ (`TenantSummary.name`)
   tình cờ vẫn khớp nên `tenant_name` từng chạy đúng, nhưng field nhiều từ
   (`CustomerSummary.fullName` ↔ JSON `full_name`, `VendorProfileSummary.businessName`/
   `serviceCategoryName`) không khớp tên → Jackson mặc định của Spring **không throw exception** khi
   thiếu/thừa property, chỉ âm thầm để `null` — nên log lỗi (`log.error("Could not fetch...")`) không hề
   xuất hiện, dễ tưởng nhầm là lỗi ở phía gọi thay vì phía parse. Fix: `InternalRestClients.create()` tự
   dựng 1 `JacksonJsonHttpMessageConverter` (Boot 4 dùng Jackson 3 — package `tools.jackson.databind.*`,
   không phải `com.fasterxml.jackson.databind.*` của Jackson 2) với `PropertyNamingStrategies.SNAKE_CASE`
   riêng, gắn vào `RestClient` thay vì dùng converter mặc định.
2. **`ResponseWrappingAdvice` throw `ClassCastException` cho mọi endpoint khai báo
   `ResponseEntity<String>`** (7 endpoint ở `TenantEventController`: accept/reject/assign/respond/
   concentrate-check-in/check-in/check-out). Spring chọn `HttpMessageConverter` dựa trên **kiểu khai báo
   ở controller** trước khi `beforeBodyWrite` chạy — với `ResponseEntity<String>`, `StringHttpMessageConverter`
   được chọn sẵn; khi advice trả về object `ApiResponse` thay vì `String`, converter cố ép kiểu và
   crash. Fix: trong `beforeBodyWrite`, nếu `selectedConverterType` là `StringHttpMessageConverter`,
   tự `objectMapper.writeValueAsString(envelope)` rồi trả chuỗi JSON đó (đồng thời set lại
   `Content-Type: application/json` vì mặc định của converter này là `text/plain`).

## Phase 7 (backend) — mở rộng theo thiết kế Figma vendor app

Figma (`LaptopHN-Software-2026`) thiết kế app vendor (Đoàn Lân Sư Rồng, phía admin + member) chi tiết hơn
nhiều so với API gốc ở Phase 1-6 — quyết định **mở rộng backend trước khi viết Flutter** để app nối thẳng
vào API thật ngay từ đầu, không phải chờ sửa lại. Phần "Admin Web / Sàn Sự Kiện Việt" trong cùng file Figma
là Phase 8 (web admin sàn), không thuộc phần này.

**identity-service:**
- `User` thêm `availabilityStatus` (`ACTIVE`/`ON_LEAVE`/`RESTING`) — badge "tạm nghỉ/dưỡng sức" ở màn quản
  lý thành viên.
- `UserController` mới (`/api/users`, ADMIN-only, scope theo tenant của JWT) — **đây là API còn thiếu hoàn
  toàn từ Phase 1**: trước giờ không có cách nào tạo tài khoản thành viên (`TN_MEMBER`) ngoài thao tác tay
  vào DB, nên "gán thành viên" ở Phase 2 chỉ test được bằng cách tái sử dụng tài khoản admin. Endpoint:
  `GET /api/users` (list + phân trang), `POST /api/users` (tạo, role mặc định `TN_MEMBER`, chỉ được chọn
  `ADMIN`/`TN_MEMBER`), `GET /api/users/{id}`, `PATCH /api/users/{id}/availability`.
- `UserContactResponse`/`IdentityServiceClient.UserContact` (internal, event-service gọi sang) thêm
  `availabilityStatus` để event-service ghép vào màn quản lý thành viên sau này.

**event-service — 2 danh mục mới theo tenant** (sống trong `event_db`, không phải catalog-service, vì đây
là cấu hình vận hành show của riêng từng tenant, không phải hồ sơ công khai):
- `CrewRole` (`/api/tenant/crew-roles`, ADMIN CRUD) — bộ phận + tên vị trí biểu diễn tự định nghĩa
  (VD bộ phận "Múa Lân" → vị trí "Đầu Lân 1"), thay cho gõ tay `position` tự do.
- `ShowPackage` (`/api/tenant/show-packages`, ADMIN CRUD) — gói biểu diễn tự định nghĩa (tên/mô tả/giá),
  chọn khi tạo show.

**`Event` thêm:** `packageId`/`packageName` (chọn từ `ShowPackage`, tên denormalize để không vỡ nếu gói bị
sửa/xoá sau), `depositAmount`, `vehicleInfo`, `venueLat`/`venueLng`/`checkinRadiusMeters` (toạ độ điểm diễn
thật + bán kính cho phép check-in, mặc định 100m nếu để trống). `EventResponse` trả thêm `depositPercent`
tính từ `depositAmount / totalAmount`.

**`UserEvent` thêm:**
- `crewRoleId` — `POST .../assign` giờ nhận thêm `crew_role_id` (tuỳ chọn); nếu có và không gửi kèm
  `position`, tên vị trí trong catalog tự trở thành `position` hiển thị. Response (`AssignmentResponse` +
  `Teammate`) trả kèm `crew_role_department`/`crew_role_name`.
- `checkinLat`/`checkinLng` — `POST .../check-in` nhận thêm `lat`/`lng` (tuỳ chọn). Nếu `Event` có cấu hình
  toạ độ điểm diễn, service tính khoảng cách (Haversine, `GeoUtils`) và **từ chối check-in** nếu vượt bán
  kính cho phép (`400` kèm khoảng cách thực tế trong message). Không cấu hình toạ độ → bỏ qua kiểm tra,
  giữ tương thích ngược với show cũ.
- `payrollItems` (entity con `UserEventPayrollItem`: label + amount) — thay cho 1 field `salary` cứng, cho
  phép liệt kê lương chính/thưởng/phụ cấp/lì xì linh hoạt như Figma. `PATCH
  .../assignments/{id}/payroll` (ADMIN, body là mảng `{label, amount}`) ghi đè toàn bộ danh sách và tự tính
  lại `salary` = tổng các item — dashboard thu nhập (`sumTotalEarnings`) không cần sửa vì vẫn cộng cùng
  cột `salary` như trước.

Đã verify end-to-end bằng curl: tạo member mới qua `UserController` → tạo crew role + show package → tạo
show có package/deposit/GPS → gán thành viên bằng `crewRoleId` (tự suy ra tên vị trí) → check-in ngoài bán
kính bị từ chối đúng khoảng cách, check-in trong bán kính thành công → check-out → set payroll 4 dòng ra
đúng tổng → dashboard thành viên cộng đúng tổng thu nhập mới.

### 2 vấn đề khác phát hiện khi nối app Flutter thật vào (không phải lúc test bằng curl)

1. **Thiếu route cho 3 endpoint mới trong `RouteTable`** — `/api/users/**`,
   `/api/tenant/crew-roles/**`, `/api/tenant/show-packages/**` chưa được thêm vào gateway khi thêm
   controller ở lần trước, nên gọi qua gateway (`:8080`) ra 404 dù gọi thẳng service (`:8081`/`:8083`)
   vẫn đúng — curl trực tiếp từng service không phát hiện ra vì luôn test bỏ qua gateway.
2. **`AssignmentResponse.status` chỉ trả chuỗi tiếng Việt đã format (`"Đang mời"`), không có field enum
   gốc** — khác với `EventResponse` (có cả `status` enum lẫn `statusDisplayName`). App Flutter so sánh
   trạng thái để hiện nút hành động (nhận show/check-in/check-out) dựa theo enum gốc nên không nút nào
   hiện ra được. Sửa: `AssignmentResponse`/`Teammate` giờ có cả `status` (enum `AssignStatus`) lẫn
   `statusDisplayName` (chuỗi hiển thị), giống hệt quy ước của `EventResponse`.
3. **Gateway thiếu CORS** — chưa cần vì trước giờ chỉ gọi bằng curl/app native. Flutter web (dùng để
   test nhanh qua trình duyệt, không cần simulator) bị chặn bởi CORS preflight. Thêm xử lý `OPTIONS` +
   header `Access-Control-Allow-*` ngay trong `GatewayProxyFilter` (wildcard origin — auth ở đây luôn là
   Bearer header do client tự đính kèm, không phải cookie, nên không có rủi ro CSRF của wildcard CORS).
   Cũng cần cho web admin sàn ở Phase 8 sau này.

## Ghi chú bảo mật

- Không commit `.env`. `.env.example` chỉ chứa placeholder.
- `INTERNAL_SERVICE_TOKEN` phải là chuỗi dài ngẫu nhiên, giống nhau trên mọi service (và trong
  `docker-compose` qua `x-app-env`).
- Mật khẩu SMTP Gmail dùng ở `SMTP_USERNAME`/`SMTP_PASSWORD` phải là App Password **mới**, khác với
  cái đã lộ plaintext trong `BE_Event_Platform/src/main/resources/application.properties` (đã bị
  commit lên git) — cái cũ coi như đã lộ, phải revoke trong Google Account trước khi tạo cái thay thế.
