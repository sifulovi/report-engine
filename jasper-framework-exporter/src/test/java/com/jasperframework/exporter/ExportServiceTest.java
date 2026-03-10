package com.jasperframework.exporter;

import com.jasperframework.core.ExportFormat;
import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportContext;
import com.jasperframework.core.ReportExecutor;
import com.jasperframework.core.ReportTemplate;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExportServiceTest {

    private static JasperPrint filledReport;
    private ExportService exportService;

    @BeforeAll
    static void compileAndFill() {
        ReportCompiler compiler = new ReportCompiler();
        ReportExecutor executor = new ReportExecutor();
        ReportTemplate template = compiler.compile("reports/simple.jrxml");
        ReportContext context = ReportContext.builder("reports/simple.jrxml")
                .parameter("title", "Export Test")
                .build();
        filledReport = executor.execute(template, context);
    }

    @BeforeEach
    void setUp() {
        exportService = new ExportService();
    }

    @Test
    void exportToBytes_pdf_shouldProduceNonEmptyOutput() {
        byte[] pdf = exportService.exportToBytes(filledReport, ExportFormat.PDF);

        assertThat(pdf).isNotEmpty();
        // PDF magic bytes: %PDF
        assertThat(pdf[0]).isEqualTo((byte) '%');
        assertThat(pdf[1]).isEqualTo((byte) 'P');
        assertThat(pdf[2]).isEqualTo((byte) 'D');
        assertThat(pdf[3]).isEqualTo((byte) 'F');
    }

    @Test
    void exportToBytes_xlsx_shouldProduceNonEmptyOutput() {
        byte[] xlsx = exportService.exportToBytes(filledReport, ExportFormat.XLSX);

        assertThat(xlsx).isNotEmpty();
        // XLSX is a ZIP (PK magic bytes)
        assertThat(xlsx[0]).isEqualTo((byte) 'P');
        assertThat(xlsx[1]).isEqualTo((byte) 'K');
    }

    @Test
    void exportToBytes_csv_shouldProduceNonEmptyOutput() {
        byte[] csv = exportService.exportToBytes(filledReport, ExportFormat.CSV);

        assertThat(csv).isNotEmpty();
    }

    @Test
    void export_shouldWriteToOutputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportService.export(filledReport, ExportFormat.PDF, baos);

        assertThat(baos.toByteArray()).isNotEmpty();
    }

    @Test
    void supportsFormat_shouldReturnTrueForRegisteredFormats() {
        assertThat(exportService.supportsFormat(ExportFormat.PDF)).isTrue();
        assertThat(exportService.supportsFormat(ExportFormat.XLSX)).isTrue();
        assertThat(exportService.supportsFormat(ExportFormat.CSV)).isTrue();
    }

    @Test
    void getExporter_shouldReturnCorrectExporter() {
        assertThat(exportService.getExporter(ExportFormat.PDF)).isInstanceOf(PdfExporter.class);
        assertThat(exportService.getExporter(ExportFormat.XLSX)).isInstanceOf(XlsxExporter.class);
        assertThat(exportService.getExporter(ExportFormat.CSV)).isInstanceOf(CsvExporter.class);
    }

    @Test
    void register_shouldReplaceExistingExporter() {
        ReportExporter custom = new ReportExporter() {
            @Override
            public void export(JasperPrint print, java.io.OutputStream output) {}

            @Override
            public ExportFormat getFormat() {
                return ExportFormat.PDF;
            }
        };

        exportService.register(custom);
        assertThat(exportService.getExporter(ExportFormat.PDF)).isSameAs(custom);
    }

    @Test
    void exportToBytes_withPageRangeAll_exportsAllPages() {
        byte[] full = exportService.exportToBytes(filledReport, ExportFormat.PDF);
        byte[] allRange = exportService.exportToBytes(filledReport, ExportFormat.PDF, PageRange.all());

        // PDFs contain unique IDs per export, so compare size not exact bytes
        assertThat(allRange).hasSameSizeAs(full);
        assertThat(allRange[0]).isEqualTo((byte) '%');
    }

    @Test
    void exportToBytes_withSinglePage_producesOutput() {
        byte[] page = exportService.exportToBytes(filledReport, ExportFormat.PDF, PageRange.single(0));
        assertThat(page).isNotEmpty();
        assertThat(page[0]).isEqualTo((byte) '%');
    }

    @Test
    void exportToBytes_withNullPageRange_exportsAll() {
        byte[] full = exportService.exportToBytes(filledReport, ExportFormat.PDF);
        byte[] nullRange = exportService.exportToBytes(filledReport, ExportFormat.PDF, null);

        assertThat(nullRange).hasSameSizeAs(full);
    }

    @Test
    void exportToBytes_startPageBeyondTotal_throws() {
        assertThatThrownBy(() ->
                exportService.exportToBytes(filledReport, ExportFormat.PDF, PageRange.of(999, 1000)))
                .isInstanceOf(ReportExportException.class)
                .hasMessageContaining("exceeds total pages");
    }
}
