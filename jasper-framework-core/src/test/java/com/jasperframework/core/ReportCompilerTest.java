package com.jasperframework.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportCompilerTest {

    private ReportCompiler compiler;

    @BeforeEach
    void setUp() {
        compiler = new ReportCompiler();
    }

    @Test
    void compile_shouldReturnCompiledTemplate() {
        ReportTemplate template = compiler.compile("reports/simple.jrxml");

        assertThat(template).isNotNull();
        assertThat(template.getTemplatePath()).isEqualTo("reports/simple.jrxml");
        assertThat(template.getCompiledReport()).isNotNull();
        assertThat(template.getCompiledReport().getName()).isEqualTo("simple");
    }

    @Test
    void compile_shouldCacheTemplate() {
        ReportTemplate first = compiler.compile("reports/simple.jrxml");
        ReportTemplate second = compiler.compile("reports/simple.jrxml");

        assertThat(first).isSameAs(second);
        assertThat(compiler.cacheSize()).isEqualTo(1);
    }

    @Test
    void compile_shouldThrowForMissingTemplate() {
        assertThatThrownBy(() -> compiler.compile("reports/nonexistent.jrxml"))
                .isInstanceOf(ReportCompilationException.class)
                .hasMessageContaining("not found on classpath");
    }

    @Test
    void evict_shouldRemoveFromCache() {
        compiler.compile("reports/simple.jrxml");
        assertThat(compiler.cacheSize()).isEqualTo(1);

        compiler.evict("reports/simple.jrxml");
        assertThat(compiler.cacheSize()).isZero();
        assertThat(compiler.getCached("reports/simple.jrxml")).isNull();
    }

    @Test
    void clearCache_shouldRemoveAllEntries() {
        compiler.compile("reports/simple.jrxml");
        compiler.clearCache();

        assertThat(compiler.cacheSize()).isZero();
    }
}
