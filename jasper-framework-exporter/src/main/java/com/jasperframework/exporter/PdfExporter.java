package com.jasperframework.exporter;

import com.jasperframework.core.ExportFormat;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * Exports a filled report to PDF format.
 */
public class PdfExporter implements ReportExporter {

    private static final Logger log = LoggerFactory.getLogger(PdfExporter.class);

    @Override
    public void export(JasperPrint print, OutputStream output) {
        log.debug("Exporting report '{}' to PDF", print.getName());
        try {
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.exportReport();
            log.info("Exported report '{}' to PDF", print.getName());
        } catch (JRException e) {
            throw new ReportExportException("Failed to export report to PDF: " + print.getName(), e);
        }
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.PDF;
    }
}
