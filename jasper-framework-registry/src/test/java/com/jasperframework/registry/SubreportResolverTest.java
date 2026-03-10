package com.jasperframework.registry;

import com.jasperframework.core.ReportCompilationException;
import com.jasperframework.core.ReportCompiler;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubreportResolverTest {

    private ReportCompiler compiler;
    private SubreportResolver resolver;

    @BeforeEach
    void setUp() {
        compiler = new ReportCompiler();
        resolver = new SubreportResolver(compiler);
    }

    @Test
    void resolveSubreports_shouldReturnCompiledSubreports() {
        ReportDefinition def = ReportDefinition.builder("MAIN", "reports/main.jrxml")
                .subreport("ITEMS_SUBREPORT", "reports/items.jrxml")
                .build();

        Map<String, Object> params = resolver.resolveSubreports(def);

        assertThat(params).hasSize(1);
        assertThat(params).containsKey("ITEMS_SUBREPORT");
        assertThat(params.get("ITEMS_SUBREPORT")).isInstanceOf(JasperReport.class);

        JasperReport subreport = (JasperReport) params.get("ITEMS_SUBREPORT");
        assertThat(subreport.getName()).isEqualTo("items");
    }

    @Test
    void resolveSubreports_shouldReturnEmptyMapWhenNoSubreports() {
        ReportDefinition def = ReportDefinition.builder("SIMPLE", "reports/main.jrxml")
                .build();

        Map<String, Object> params = resolver.resolveSubreports(def);

        assertThat(params).isEmpty();
    }

    @Test
    void resolveSubreports_shouldThrowForMissingSubreport() {
        ReportDefinition def = ReportDefinition.builder("MAIN", "reports/main.jrxml")
                .subreport("MISSING_SUB", "reports/nonexistent.jrxml")
                .build();

        assertThatThrownBy(() -> resolver.resolveSubreports(def))
                .isInstanceOf(ReportCompilationException.class);
    }

    @Test
    void resolveSubreports_shouldCacheCompiledSubreports() {
        ReportDefinition def = ReportDefinition.builder("MAIN", "reports/main.jrxml")
                .subreport("ITEMS_SUBREPORT", "reports/items.jrxml")
                .build();

        resolver.resolveSubreports(def);
        resolver.resolveSubreports(def);

        // The compiler should have cached the subreport template
        assertThat(compiler.getCached("reports/items.jrxml")).isNotNull();
        assertThat(compiler.cacheSize()).isEqualTo(1);
    }

    @Test
    void constructor_shouldRejectNullCompiler() {
        assertThatThrownBy(() -> new SubreportResolver(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
