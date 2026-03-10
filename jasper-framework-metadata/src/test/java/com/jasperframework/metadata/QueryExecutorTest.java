package com.jasperframework.metadata;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QueryExecutorTest {

    private static JdbcDataSource dataSource;
    private static QueryExecutor executor;

    @BeforeAll
    static void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:query_test;DB_CLOSE_DELAY=-1");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE orders (id BIGINT, customer_id BIGINT, total DECIMAL(10,2))");
            stmt.execute("INSERT INTO orders VALUES (1, 100, 250.00)");
            stmt.execute("INSERT INTO orders VALUES (2, 100, 150.00)");
            stmt.execute("INSERT INTO orders VALUES (3, 200, 300.00)");
        }

        executor = new QueryExecutor(dataSource);
    }

    @Test
    void execute_shouldReturnDataSource() throws JRException {
        JRDataSource ds = executor.execute(
                "SELECT id, customer_id, total FROM orders WHERE customer_id = :customerId",
                Map.of("customerId", 100L));

        assertThat(ds).isNotNull();

        // Verify we can iterate the data source
        int count = 0;
        while (ds.next()) {
            count++;
        }
        assertThat(count).isEqualTo(2);
    }

    @Test
    void execute_shouldReturnAllRows() throws JRException {
        JRDataSource ds = executor.execute("SELECT * FROM orders", Map.of());

        int count = 0;
        while (ds.next()) {
            count++;
        }
        assertThat(count).isEqualTo(3);
    }
}
