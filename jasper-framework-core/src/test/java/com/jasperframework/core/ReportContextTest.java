package com.jasperframework.core;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportContextTest {

    @Test
    void builder_shouldCreateContextWithParameters() {
        ReportContext ctx = ReportContext.builder("reports/test.jrxml")
                .parameter("key1", "value1")
                .parameter("key2", 42)
                .build();

        assertThat(ctx.getTemplatePath()).isEqualTo("reports/test.jrxml");
        assertThat(ctx.getParameters())
                .containsEntry("key1", "value1")
                .containsEntry("key2", 42);
        assertThat(ctx.hasConnection()).isFalse();
        assertThat(ctx.hasDataSource()).isFalse();
    }

    @Test
    void builder_shouldAcceptBulkParameters() {
        Map<String, Object> params = Map.of("a", 1, "b", 2);

        ReportContext ctx = ReportContext.builder("reports/test.jrxml")
                .parameters(params)
                .build();

        assertThat(ctx.getParameters()).hasSize(2);
    }

    @Test
    void parameters_shouldBeImmutable() {
        ReportContext ctx = ReportContext.builder("reports/test.jrxml")
                .parameter("key", "value")
                .build();

        assertThatThrownBy(() -> ctx.getParameters().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void builder_shouldRejectBlankTemplatePath() {
        assertThatThrownBy(() -> ReportContext.builder(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ReportContext.builder(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
