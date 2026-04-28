package org.ntnu.backend.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class SchemaFixRunnerTest {

    private JdbcTemplate jdbcTemplate;
    private SchemaFixRunner schemaFixRunner;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(java.sql.Connection.class));

        // Use constructor injection via spy trick — we inject a mock JdbcTemplate instead
        schemaFixRunner = new SchemaFixRunner(dataSource) {
            // Override run to use our mock
        };

        // Inject mocked JdbcTemplate via reflection
        jdbcTemplate = mock(JdbcTemplate.class);
        java.lang.reflect.Field field = SchemaFixRunner.class.getDeclaredField("jdbc");
        field.setAccessible(true);
        field.set(schemaFixRunner, jdbcTemplate);
    }

    @Test
    void run_altersAppUserColumnToNullable() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(Collections.emptyList());

        schemaFixRunner.run(mock(ApplicationArguments.class));

        verify(jdbcTemplate).execute(contains("MODIFY username VARCHAR(255) NULL"));
    }

    @Test
    void run_altersNotificationTypeColumn() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(Collections.emptyList());

        schemaFixRunner.run(mock(ApplicationArguments.class));

        verify(jdbcTemplate).execute(contains("MODIFY COLUMN type VARCHAR(50) NOT NULL"));
    }

    @Test
    void run_dropsUniqueIndexesOnUsernameColumn() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("UK_username_idx"));

        schemaFixRunner.run(mock(ApplicationArguments.class));

        verify(jdbcTemplate).execute(contains("DROP INDEX `UK_username_idx`"));
    }

    @Test
    void run_doesNotDropIndexWhenNoneExist() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(Collections.emptyList());

        schemaFixRunner.run(mock(ApplicationArguments.class));

        verify(jdbcTemplate, never()).execute(contains("DROP INDEX"));
    }

    @Test
    void run_dropsMultipleIndexesWhenPresent() throws Exception {
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(List.of("idx_one", "idx_two"));

        schemaFixRunner.run(mock(ApplicationArguments.class));

        verify(jdbcTemplate).execute(contains("DROP INDEX `idx_one`"));
        verify(jdbcTemplate).execute(contains("DROP INDEX `idx_two`"));
    }
}