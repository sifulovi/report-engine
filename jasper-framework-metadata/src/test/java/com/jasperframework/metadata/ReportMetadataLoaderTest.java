package com.jasperframework.metadata;

import com.jasperframework.core.ReportException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportMetadataLoaderTest {

    private static JdbcDataSource dataSource;
    private static ReportMetadataLoader loader;

    @BeforeAll
    static void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:metadata_test;DB_CLOSE_DELAY=-1");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE reports ("
                    + "code VARCHAR(50) PRIMARY KEY, name VARCHAR(200), "
                    + "description TEXT, template_path VARCHAR(500), "
                    + "query TEXT, active BOOLEAN DEFAULT TRUE)");

            stmt.execute("CREATE TABLE report_parameters ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "report_code VARCHAR(50), name VARCHAR(100), "
                    + "type VARCHAR(100) DEFAULT 'java.lang.String', "
                    + "required BOOLEAN DEFAULT FALSE, default_value VARCHAR(500))");

            stmt.execute("CREATE TABLE report_subreports ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "report_code VARCHAR(50), parameter_name VARCHAR(100), "
                    + "template_path VARCHAR(500))");

            stmt.execute("INSERT INTO reports VALUES ('INV-001', 'Invoice', 'Invoice report', "
                    + "'reports/invoice.jrxml', 'SELECT * FROM invoices WHERE id = :id', true)");

            stmt.execute("INSERT INTO report_parameters VALUES "
                    + "(1, 'INV-001', 'id', 'java.lang.Long', true, null)");
            stmt.execute("INSERT INTO report_parameters VALUES "
                    + "(2, 'INV-001', 'format', 'java.lang.String', false, 'PDF')");

            stmt.execute("INSERT INTO report_subreports VALUES "
                    + "(1, 'INV-001', 'ITEMS_SUBREPORT', 'reports/invoice_items.jrxml')");

            stmt.execute("INSERT INTO reports VALUES ('INACTIVE', 'Inactive', null, "
                    + "'reports/inactive.jrxml', null, false)");
        }

        loader = new ReportMetadataLoader(dataSource);
    }

    @Test
    void load_shouldReturnFullMetadata() {
        ReportMetadata meta = loader.load("INV-001");

        assertThat(meta.getCode()).isEqualTo("INV-001");
        assertThat(meta.getName()).isEqualTo("Invoice");
        assertThat(meta.getTemplatePath()).isEqualTo("reports/invoice.jrxml");
        assertThat(meta.getQuery()).contains("SELECT");
        assertThat(meta.isActive()).isTrue();
        assertThat(meta.getParameters()).hasSize(2);
        assertThat(meta.getSubreports()).hasSize(1);
    }

    @Test
    void load_shouldMapParameters() {
        ReportMetadata meta = loader.load("INV-001");
        ReportParameterMetadata param = meta.getParameters().stream()
                .filter(p -> p.getName().equals("id"))
                .findFirst().orElseThrow();

        assertThat(param.getType()).isEqualTo("java.lang.Long");
        assertThat(param.isRequired()).isTrue();
        assertThat(param.getDefaultValue()).isNull();
    }

    @Test
    void load_shouldMapSubreports() {
        ReportMetadata meta = loader.load("INV-001");
        ReportSubreportMetadata sub = meta.getSubreports().get(0);

        assertThat(sub.getParameterName()).isEqualTo("ITEMS_SUBREPORT");
        assertThat(sub.getTemplatePath()).isEqualTo("reports/invoice_items.jrxml");
    }

    @Test
    void load_shouldThrowForUnknownReport() {
        assertThatThrownBy(() -> loader.load("UNKNOWN"))
                .isInstanceOf(ReportException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void loadAllActive_shouldReturnOnlyActiveReports() {
        var active = loader.loadAllActive();

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getCode()).isEqualTo("INV-001");
    }
}
