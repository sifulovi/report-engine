package com.jasperframework.metadata;

import com.jasperframework.core.ReportException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes SQL queries with named parameter support and converts results
 * to {@link JRMapCollectionDataSource} for JasperReports consumption.
 * <p>
 * Named parameters use the {@code :paramName} syntax.
 *
 * <pre>{@code
 * QueryExecutor executor = new QueryExecutor(dataSource);
 * JRDataSource ds = executor.execute(
 *         "SELECT * FROM orders WHERE customer_id = :customerId",
 *         Map.of("customerId", 42));
 * }</pre>
 */
public class QueryExecutor {

    private static final Logger log = LoggerFactory.getLogger(QueryExecutor.class);
    private static final Pattern NAMED_PARAM = Pattern.compile(":(\\w+)");

    private final DataSource dataSource;

    public QueryExecutor(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource must not be null");
        }
        this.dataSource = dataSource;
    }

    /**
     * Executes a SQL query with named parameters and returns the result as a JRDataSource.
     *
     * @param sql    SQL with named parameters (e.g. {@code :customerId})
     * @param params parameter values keyed by name
     * @return data source containing the query results
     */
    public JRDataSource execute(String sql, Map<String, Object> params) {
        ParsedQuery parsed = parseNamedParameters(sql);
        log.debug("Executing query with {} parameters", params.size());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(parsed.sql)) {

            // Bind parameters in order
            for (int i = 0; i < parsed.parameterNames.size(); i++) {
                String paramName = parsed.parameterNames.get(i);
                Object value = params.get(paramName);
                stmt.setObject(i + 1, value);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, ?>> rows = mapResultSet(rs);
                log.info("Query returned {} rows", rows.size());
                return new JRMapCollectionDataSource(rows);
            }
        } catch (SQLException e) {
            throw new ReportException("Failed to execute report query", e);
        }
    }

    private List<Map<String, ?>> mapResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        List<Map<String, ?>> rows = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String label = meta.getColumnLabel(i);
                row.put(label, rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * Replaces {@code :paramName} tokens with {@code ?} and tracks parameter order.
     */
    static ParsedQuery parseNamedParameters(String sql) {
        List<String> paramNames = new ArrayList<>();
        Matcher matcher = NAMED_PARAM.matcher(sql);
        StringBuilder parsed = new StringBuilder();
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
            matcher.appendReplacement(parsed, "?");
        }
        matcher.appendTail(parsed);
        return new ParsedQuery(parsed.toString(), paramNames);
    }

    static class ParsedQuery {
        final String sql;
        final List<String> parameterNames;

        ParsedQuery(String sql, List<String> parameterNames) {
            this.sql = sql;
            this.parameterNames = parameterNames;
        }
    }
}
