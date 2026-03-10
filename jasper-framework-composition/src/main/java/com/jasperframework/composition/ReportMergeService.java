package com.jasperframework.composition;

import com.jasperframework.core.ExportFormat;
import com.jasperframework.exporter.ExportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.List;

/**
 * Higher-level service that composes multiple reports and exports the result in one step.
 *
 * <pre>{@code
 * ReportMergeService mergeService = new ReportMergeService(
 *         new ReportComposer(), new ExportService());
 *
 * byte[] pdf = mergeService.mergeAndExport("Financial Report",
 *         ExportFormat.PDF, balanceSheet, incomeStatement, cashFlow);
 * }</pre>
 */
public class ReportMergeService {

    private static final Logger log = LoggerFactory.getLogger(ReportMergeService.class);

    private final ReportComposer composer;
    private final ExportService exportService;

    public ReportMergeService(ReportComposer composer, ExportService exportService) {
        if (composer == null) {
            throw new IllegalArgumentException("composer must not be null");
        }
        if (exportService == null) {
            throw new IllegalArgumentException("exportService must not be null");
        }
        this.composer = composer;
        this.exportService = exportService;
    }

    /**
     * Composes multiple reports into one and exports to the given format.
     */
    public void mergeAndExport(String name, ExportFormat format,
                               OutputStream output, JasperPrint... reports) {
        JasperPrint merged = composer.compose(name, reports);
        exportService.export(merged, format, output);
    }

    /**
     * Composes multiple reports into one and exports to bytes.
     */
    public byte[] mergeAndExport(String name, ExportFormat format, JasperPrint... reports) {
        JasperPrint merged = composer.compose(name, reports);
        return exportService.exportToBytes(merged, format);
    }

    /**
     * Composes multiple reports into one and exports to bytes.
     */
    public byte[] mergeAndExport(String name, ExportFormat format, List<JasperPrint> reports) {
        JasperPrint merged = composer.compose(name, reports);
        return exportService.exportToBytes(merged, format);
    }
}
