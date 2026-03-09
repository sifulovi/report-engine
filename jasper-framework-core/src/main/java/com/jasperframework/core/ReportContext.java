package com.jasperframework.core;

import net.sf.jasperreports.engine.JRDataSource;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable context holding everything needed to fill a report:
 * template path, parameters, and a data source or JDBC connection.
 * <p>
 * Use the {@link Builder} for construction.
 */
public final class ReportContext {

    private final String templatePath;
    private final Map<String, Object> parameters;
    private final JRDataSource dataSource;
    private final Connection connection;

    private ReportContext(Builder builder) {
        this.templatePath = builder.templatePath;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(builder.parameters));
        this.dataSource = builder.dataSource;
        this.connection = builder.connection;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public JRDataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean hasConnection() {
        return connection != null;
    }

    public boolean hasDataSource() {
        return dataSource != null;
    }

    public static Builder builder(String templatePath) {
        return new Builder(templatePath);
    }

    public static final class Builder {

        private final String templatePath;
        private final Map<String, Object> parameters = new HashMap<>();
        private JRDataSource dataSource;
        private Connection connection;

        private Builder(String templatePath) {
            if (templatePath == null || templatePath.isBlank()) {
                throw new IllegalArgumentException("templatePath must not be blank");
            }
            this.templatePath = templatePath;
        }

        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder parameters(Map<String, Object> params) {
            if (params != null) {
                this.parameters.putAll(params);
            }
            return this;
        }

        public Builder dataSource(JRDataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        public ReportContext build() {
            return new ReportContext(this);
        }
    }
}
