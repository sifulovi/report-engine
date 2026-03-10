package com.jasperframework.metadata;

import com.jasperframework.core.ReportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads report metadata from the database using plain JDBC.
 * <p>
 * Expected schema:
 * <pre>
 * CREATE TABLE reports (
 *     code          VARCHAR(50) PRIMARY KEY,
 *     name          VARCHAR(200) NOT NULL,
 *     description   TEXT,
 *     template_path VARCHAR(500) NOT NULL,
 *     query         TEXT,
 *     active        BOOLEAN DEFAULT TRUE
 * );
 *
 * CREATE TABLE report_parameters (
 *     id            BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     report_code   VARCHAR(50) NOT NULL REFERENCES reports(code),
 *     name          VARCHAR(100) NOT NULL,
 *     type          VARCHAR(100) NOT NULL DEFAULT 'java.lang.String',
 *     required      BOOLEAN DEFAULT FALSE,
 *     default_value VARCHAR(500)
 * );
 *
 * CREATE TABLE report_subreports (
 *     id              BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     report_code     VARCHAR(50) NOT NULL REFERENCES reports(code),
 *     parameter_name  VARCHAR(100) NOT NULL,
 *     template_path   VARCHAR(500) NOT NULL
 * );
 * </pre>
 */
public class ReportMetadataLoader {

    private static final Logger log = LoggerFactory.getLogger(ReportMetadataLoader.class);

    private static final String SELECT_REPORT =
            "SELECT code, name, description, template_path, query, active FROM reports WHERE code = ?";
    private static final String SELECT_PARAMETERS =
            "SELECT name, type, required, default_value FROM report_parameters WHERE report_code = ?";
    private static final String SELECT_SUBREPORTS =
            "SELECT parameter_name, template_path FROM report_subreports WHERE report_code = ?";
    private static final String SELECT_ALL_ACTIVE =
            "SELECT code, name, description, template_path, query, active FROM reports WHERE active = true";

    private final DataSource dataSource;

    public ReportMetadataLoader(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null");
        }
        this.dataSource = dataSource;
    }

    /**
     * Loads a single report's full metadata (including parameters and subreports).
     *
     * @throws ReportException if the report is not found or a database error occurs
     */
    public ReportMetadata load(String reportCode) {
        log.debug("Loading metadata for report: {}", reportCode);
        try (Connection conn = dataSource.getConnection()) {
            ReportMetadata metadata = loadReport(conn, reportCode);
            if (metadata == null) {
                throw new ReportException("Report not found in metadata: " + reportCode);
            }
            metadata.setParameters(loadParameters(conn, reportCode));
            metadata.setSubreports(loadSubreports(conn, reportCode));
            log.info("Loaded metadata for report: {} ({} params, {} subreports)",
                    reportCode, metadata.getParameters().size(), metadata.getSubreports().size());
            return metadata;
        } catch (SQLException e) {
            throw new ReportException("Failed to load metadata for report: " + reportCode, e);
        }
    }

    /**
     * Loads all active report metadata records (without parameters/subreports).
     */
    public List<ReportMetadata> loadAllActive() {
        log.debug("Loading all active report metadata");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVE);
             ResultSet rs = stmt.executeQuery()) {

            List<ReportMetadata> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapReport(rs));
            }
            log.info("Loaded {} active report metadata records", results.size());
            return results;
        } catch (SQLException e) {
            throw new ReportException("Failed to load active report metadata", e);
        }
    }

    private ReportMetadata loadReport(Connection conn, String reportCode) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_REPORT)) {
            stmt.setString(1, reportCode);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapReport(rs) : null;
            }
        }
    }

    private List<ReportParameterMetadata> loadParameters(Connection conn, String reportCode) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_PARAMETERS)) {
            stmt.setString(1, reportCode);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReportParameterMetadata> params = new ArrayList<>();
                while (rs.next()) {
                    ReportParameterMetadata param = new ReportParameterMetadata();
                    param.setName(rs.getString("name"));
                    param.setType(rs.getString("type"));
                    param.setRequired(rs.getBoolean("required"));
                    param.setDefaultValue(rs.getString("default_value"));
                    params.add(param);
                }
                return params;
            }
        }
    }

    private List<ReportSubreportMetadata> loadSubreports(Connection conn, String reportCode) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_SUBREPORTS)) {
            stmt.setString(1, reportCode);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ReportSubreportMetadata> subs = new ArrayList<>();
                while (rs.next()) {
                    ReportSubreportMetadata sub = new ReportSubreportMetadata();
                    sub.setParameterName(rs.getString("parameter_name"));
                    sub.setTemplatePath(rs.getString("template_path"));
                    subs.add(sub);
                }
                return subs;
            }
        }
    }

    private ReportMetadata mapReport(ResultSet rs) throws SQLException {
        ReportMetadata m = new ReportMetadata();
        m.setCode(rs.getString("code"));
        m.setName(rs.getString("name"));
        m.setDescription(rs.getString("description"));
        m.setTemplatePath(rs.getString("template_path"));
        m.setQuery(rs.getString("query"));
        m.setActive(rs.getBoolean("active"));
        return m;
    }
}
