-- Chạy trên MySQL localhost (không cần Docker).
-- Ví dụ:
--   mysql -u root -p < infra/mysql/local-create-databases.sql
-- hoặc paste vào DataGrip / MySQL Workbench khi đang connect localhost:3306.
--
-- Mỗi microservice một schema (database-per-service).
-- User dưới đây dùng chung cho mọi service — đổi username/password cho khớp .env.

CREATE DATABASE IF NOT EXISTS identity_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS catalog_db      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS event_db        CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS customer_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Nếu dùng user riêng (không phải root), bỏ comment và sửa cho khớp DB_USERNAME / DB_PASSWORD trong .env:
-- CREATE USER IF NOT EXISTS 'app_user'@'localhost' IDENTIFIED BY 'changeme_app';
-- CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'changeme_app';
-- GRANT ALL PRIVILEGES ON identity_db.*     TO 'app_user'@'localhost';
-- GRANT ALL PRIVILEGES ON catalog_db.*      TO 'app_user'@'localhost';
-- GRANT ALL PRIVILEGES ON event_db.*        TO 'app_user'@'localhost';
-- GRANT ALL PRIVILEGES ON customer_db.*     TO 'app_user'@'localhost';
-- GRANT ALL PRIVILEGES ON notification_db.* TO 'app_user'@'localhost';
-- GRANT ALL PRIVILEGES ON identity_db.*     TO 'app_user'@'%';
-- GRANT ALL PRIVILEGES ON catalog_db.*      TO 'app_user'@'%';
-- GRANT ALL PRIVILEGES ON event_db.*        TO 'app_user'@'%';
-- GRANT ALL PRIVILEGES ON customer_db.*     TO 'app_user'@'%';
-- GRANT ALL PRIVILEGES ON notification_db.* TO 'app_user'@'%';
-- FLUSH PRIVILEGES;

SHOW DATABASES LIKE '%_db';
