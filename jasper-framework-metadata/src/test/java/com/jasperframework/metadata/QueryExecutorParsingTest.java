package com.jasperframework.metadata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryExecutorParsingTest {

    @Test
    void parseNamedParameters_shouldReplaceNamedParams() {
        QueryExecutor.ParsedQuery parsed = QueryExecutor.parseNamedParameters(
                "SELECT * FROM orders WHERE customer_id = :customerId AND status = :status");

        assertThat(parsed.sql).isEqualTo(
                "SELECT * FROM orders WHERE customer_id = ? AND status = ?");
        assertThat(parsed.parameterNames).containsExactly("customerId", "status");
    }

    @Test
    void parseNamedParameters_shouldHandleNoParams() {
        QueryExecutor.ParsedQuery parsed = QueryExecutor.parseNamedParameters(
                "SELECT * FROM orders");

        assertThat(parsed.sql).isEqualTo("SELECT * FROM orders");
        assertThat(parsed.parameterNames).isEmpty();
    }

    @Test
    void parseNamedParameters_shouldHandleDuplicateParams() {
        QueryExecutor.ParsedQuery parsed = QueryExecutor.parseNamedParameters(
                "SELECT * FROM orders WHERE id = :id OR parent_id = :id");

        assertThat(parsed.sql).isEqualTo(
                "SELECT * FROM orders WHERE id = ? OR parent_id = ?");
        assertThat(parsed.parameterNames).containsExactly("id", "id");
    }
}
