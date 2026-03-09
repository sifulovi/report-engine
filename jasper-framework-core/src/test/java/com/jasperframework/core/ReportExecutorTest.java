package com.jasperframework.core;

import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportExecutorTest {

    private ReportCompiler compiler;
    private ReportExecutor executor;

    @BeforeEach
    void setUp() {
        compiler = new ReportCompiler();
        executor = new ReportExecutor();
    }

    @Test
    void execute_shouldFillReportWithParameters() {
        ReportTemplate template = compiler.compile("reports/simple.jrxml");

        ReportContext context = ReportContext.builder("reports/simple.jrxml")
                .parameter("title", "Test Report")
                .build();

        JasperPrint print = executor.execute(template, context);

        assertThat(print).isNotNull();
        assertThat(print.getName()).isEqualTo("simple");
        assertThat(print.getPages()).isNotEmpty();
    }

    @Test
    void execute_shouldFillReportWithoutDataSource() {
        ReportTemplate template = compiler.compile("reports/simple.jrxml");

        ReportContext context = ReportContext.builder("reports/simple.jrxml")
                .build();

        JasperPrint print = executor.execute(template, context);

        assertThat(print).isNotNull();
    }
}
