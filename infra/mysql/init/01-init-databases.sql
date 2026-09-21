-- identity_db is created automatically by MYSQL_DATABASE / MYSQL_USER in docker-compose.yml.
-- The remaining per-service databases are created here and granted to the same app user.
-- If you change DB_USERNAME in .env, update the username literal below to match.
--
-- NOTE: this script only runs on first MySQL volume init. If the volume already exists,
-- run the CREATE/GRANT statements manually (or docker compose down -v && up) to pick up
-- new databases such as notification_db.

CREATE DATABASE IF NOT EXISTS catalog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS event_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS customer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON catalog_db.* TO 'avnadmin'@'%';
GRANT ALL PRIVILEGES ON event_db.* TO 'avnadmin'@'%';
GRANT ALL PRIVILEGES ON customer_db.* TO 'avnadmin'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'avnadmin'@'%';

FLUSH PRIVILEGES;
