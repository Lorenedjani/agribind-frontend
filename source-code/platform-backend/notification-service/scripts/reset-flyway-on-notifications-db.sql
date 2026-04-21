-- Run ONLY against database agribind_notifications (not agribind_users).
-- Use when Flyway validation fails on stale/failed migrations (e.g. version 3) after
-- spring.flyway.enabled: false is set in notification-service application.yml.
-- mysql -u root -p agribind_notifications < reset-flyway-on-notifications-db.sql
-- Alternative: mysql ... -e "DELETE FROM flyway_schema_history WHERE success = 0;" or flyway repair

DROP TABLE IF EXISTS flyway_schema_history;
