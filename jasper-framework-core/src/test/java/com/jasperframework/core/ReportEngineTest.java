package com.jasperframework.core;

import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportEngineTest {

    private ReportEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ReportEngine();
    }

    @Test
    void generateReport_shouldCompileAndFill() {
        ReportContext context = ReportContext.builder("reports/simple.jrxml")
                .parameter("title", "Invoice #100")
                .build();

        JasperPrint print = engine.generateReport(context);

        assertThat(print).isNotNull();
        assertThat(print.getName()).isEqualTo("simple");
        assertThat(print.getPages()).isNotEmpty();
    }

    @Test
    void generateReport_shouldCacheCompilation() {
        ReportContext ctx1 = ReportContext.builder("reports/simple.jrxml")
                .parameter("title", "First")
                .build();
        ReportContext ctx2 = ReportContext.builder("reports/simple.jrxml")
                .parameter("title", "Second")
                .build();

        engine.generateReport(ctx1);
        engine.generateReport(ctx2);

        assertThat(engine.getCompiler().cacheSize()).isEqualTo(1);
    }

    @Test
    void generateReport_shouldThrowForMissingTemplate() {
        ReportContext context = ReportContext.builder("reports/missing.jrxml").build();

        assertThatThrownBy(() -> engine.generateReport(context))
                .isInstanceOf(ReportCompilationException.class);
    }

    @Test
    void constructor_shouldRejectNullCompiler() {
        assertThatThrownBy(() -> new ReportEngine(null, new ReportExecutor()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_shouldRejectNullExecutor() {
        assertThatThrownBy(() -> new ReportEngine(new ReportCompiler(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
