package com.jasperframework.exporter;

import com.jasperframework.core.ExportFormat;
import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Central export service that selects the appropriate exporter based on the requested format.
 * <p>
 * Pre-registers PDF, XLSX, and CSV exporters by default. Additional exporters can be registered
 * via {@link #register(ReportExporter)}.
 *
 * <pre>{@code
 * ExportService exportService = new ExportService();
 * byte[] pdf = exportService.exportToBytes(jasperPrint, ExportFormat.PDF);
 * }</pre>
 */
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final Map<ExportFormat, ReportExporter> exporters = new EnumMap<>(ExportFormat.class);

    public ExportService() {
        register(new PdfExporter());
        register(new XlsxExporter());
        register(new CsvExporter());
    }

    /**
     * Registers (or replaces) an exporter for a given format.
     */
    public void register(ReportExporter exporter) {
        exporters.put(exporter.getFormat(), exporter);
        log.debug("Registered exporter for format: {}", exporter.getFormat());
    }

    /**
     * Exports the filled report to the given output stream.
     *
     * @param print  the filled report
     * @param format desired export format
     * @param output destination stream
     */
    public void export(JasperPrint print, ExportFormat format, OutputStream output) {
        ReportExporter exporter = getExporter(format);
        exporter.export(print, output);
    }

    /**
     * Exports the filled report and returns the result as a byte array.
     */
    public byte[] exportToBytes(JasperPrint print, ExportFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        export(print, format, baos);
        return baos.toByteArray();
    }

    /**
     * Returns the exporter for the given format.
     *
     * @throws ReportExportException if no exporter is registered for the format
     */
    public ReportExporter getExporter(ExportFormat format) {
        ReportExporter exporter = exporters.get(format);
        if (exporter == null) {
            throw new ReportExportException("No exporter registered for format: " + format);
        }
        return exporter;
    }

    /**
     * Returns {@code true} if an exporter is registered for the given format.
     */
    public boolean supportsFormat(ExportFormat format) {
        return exporters.containsKey(format);
    }
}
