package com.jasperframework.composition;

import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportContext;
import com.jasperframework.core.ReportExecutor;
import com.jasperframework.core.ReportTemplate;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportComposerTest {

    private static JasperPrint report1;
    private static JasperPrint report2;
    private final ReportComposer composer = new ReportComposer();

    @BeforeAll
    static void setUp() {
        ReportCompiler compiler = new ReportCompiler();
        ReportExecutor executor = new ReportExecutor();
        ReportTemplate template = compiler.compile("reports/simple.jrxml");

        report1 = executor.execute(template, ReportContext.builder("reports/simple.jrxml")
                .parameter("title", "Balance Sheet").build());
        report2 = executor.execute(template, ReportContext.builder("reports/simple.jrxml")
                .parameter("title", "Income Statement").build());
    }

    @Test
    void compose_shouldMergePages() {
        JasperPrint merged = composer.compose("Financial Report", report1, report2);

        assertThat(merged.getName()).isEqualTo("Financial Report");
        assertThat(merged.getPages()).hasSize(
                report1.getPages().size() + report2.getPages().size());
    }

    @Test
    void compose_shouldPreservePageDimensions() {
        JasperPrint merged = composer.compose("Merged", report1);

        assertThat(merged.getPageWidth()).isEqualTo(report1.getPageWidth());
        assertThat(merged.getPageHeight()).isEqualTo(report1.getPageHeight());
    }

    @Test
    void compose_shouldWorkWithSingleReport() {
        JasperPrint merged = composer.compose("Single", report1);

        assertThat(merged.getPages()).hasSameSizeAs(report1.getPages());
    }

    @Test
    void compose_shouldWorkWithList() {
        JasperPrint merged = composer.compose("Listed", List.of(report1, report2));

        assertThat(merged.getPages()).hasSize(
                report1.getPages().size() + report2.getPages().size());
    }

    @Test
    void compose_shouldRejectEmptyInput() {
        assertThatThrownBy(() -> composer.compose("Empty", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void compose_shouldRejectNull() {
        assertThatThrownBy(() -> composer.compose("Null", (List<JasperPrint>) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
