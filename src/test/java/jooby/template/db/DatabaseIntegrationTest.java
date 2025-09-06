package jooby.template.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashSet;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class DatabaseIntegrationTest implements TestDslContextAware {
  private static final PostgreSQLContainer<?> POSTGRESQL =
      new PostgreSQLContainer<>("postgres:17-alpine");

  private static HikariDataSource dataSource;
  private static DSLContext dsl;
  private static Set<Table<?>> tables;
  private static Set<Table<?>> materializedViews;

  @BeforeAll
  static void beforeAll() {
    // Kick off the database container
    POSTGRESQL.setCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all");
    POSTGRESQL.start();

    // Must be aligned with properties used by the application in application.conf
    System.setProperty("db.url", POSTGRESQL.getJdbcUrl());
    System.setProperty("db.user", POSTGRESQL.getUsername());
    System.setProperty("db.password", POSTGRESQL.getPassword());

    // Create database connection pool for tests
    HikariConfig hikariReadWriteConfig = new HikariConfig();
    hikariReadWriteConfig.setDriverClassName(POSTGRESQL.getDriverClassName());
    hikariReadWriteConfig.setJdbcUrl(POSTGRESQL.getJdbcUrl());
    hikariReadWriteConfig.setUsername(POSTGRESQL.getUsername());
    hikariReadWriteConfig.setPassword(POSTGRESQL.getPassword());
    dataSource = new HikariDataSource(hikariReadWriteConfig);

    // Create DSL context for direct database querying in tests
    dsl = DSL.using(dataSource, SQLDialect.POSTGRES);

    // Store user-defined tables, improves test execution speed
    tables = new HashSet<>();
    materializedViews = new HashSet<>();
    for (var table : dsl.meta().getTables()) {
      if ("public".equalsIgnoreCase(table.getSchema().getName())
          && !table.getName().startsWith("flyway")
          && !table.getName().endsWith("_view")) {
        tables.add(table);
      } else if (table.getName().endsWith("_materialized_view")) {
        // Should be adjusted for naming convention of the specific project
        materializedViews.add(table);
      }
    }
  }

  @BeforeEach
  void truncateAll() {
    if (!tables.isEmpty()) {
      dsl.truncate(tables).restartIdentity().cascade().execute();
    }

    for (final var materializedView : materializedViews) {
      dsl.execute("REFRESH MATERIALIZED VIEW " + materializedView.getQualifiedName());
    }
  }

  @AfterAll
  static void tearDown() {
    dataSource.close();
    POSTGRESQL.stop();
  }

  @Override
  public DSLContext database() {
    return dsl;
  }
}
