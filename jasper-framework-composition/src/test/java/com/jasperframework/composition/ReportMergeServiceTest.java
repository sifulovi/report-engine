package com.jasperframework.composition;

import com.jasperframework.core.ExportFormat;
import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportContext;
import com.jasperframework.core.ReportExecutor;
import com.jasperframework.core.ReportTemplate;
import com.jasperframework.exporter.ExportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportMergeServiceTest {

    private static JasperPrint report1;
    private static JasperPrint report2;

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
    void mergeAndExport_shouldProducePdf() {
        ReportMergeService service = new ReportMergeService(
                new ReportComposer(), new ExportService());

        byte[] pdf = service.mergeAndExport("Financial Report", ExportFormat.PDF, report1, report2);

        assertThat(pdf).isNotEmpty();
        // PDF magic bytes
        assertThat(pdf[0]).isEqualTo((byte) '%');
        assertThat(pdf[1]).isEqualTo((byte) 'P');
    }

    @Test
    void mergeAndExport_shouldProduceXlsx() {
        ReportMergeService service = new ReportMergeService(
                new ReportComposer(), new ExportService());

        byte[] xlsx = service.mergeAndExport("Financial Report", ExportFormat.XLSX, report1, report2);

        assertThat(xlsx).isNotEmpty();
    }
}
