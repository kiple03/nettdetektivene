package org.ntnu.backend.config;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time schema fix: relax the NOT NULL constraint on app_user.username.
 *
 * <p>Hibernate's ddl-auto=update cannot drop NOT NULL from an existing column,
 * so this runner issues the ALTER directly after startup.
 * Safe to run repeatedly — MySQL's MODIFY is idempotent.
 */
@Component
public class SchemaFixRunner implements ApplicationRunner {

  private final JdbcTemplate jdbc;

  public SchemaFixRunner(DataSource dataSource) {
    this.jdbc = new JdbcTemplate(dataSource);
  }

  @Override
  public void run(ApplicationArguments args) {
    jdbc.execute("ALTER TABLE app_user MODIFY username VARCHAR(255) NULL");
    jdbc.execute("ALTER TABLE notification MODIFY COLUMN type VARCHAR(50) NOT NULL");
    List<String> idxNames = jdbc.queryForList(
        "SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS " +
        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'app_user' " +
        "AND COLUMN_NAME = 'username' AND NON_UNIQUE = 0 AND INDEX_NAME != 'PRIMARY'",
        String.class);
    for (String idx : idxNames) {
      jdbc.execute("ALTER TABLE app_user DROP INDEX `" + idx + "`");
    }
  }
}
