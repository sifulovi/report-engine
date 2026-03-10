package com.jasperframework.exporter;

import com.jasperframework.core.ExportFormat;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * Exports a filled report to XLSX (Excel) format.
 */
public class XlsxExporter implements ReportExporter {

    private static final Logger log = LoggerFactory.getLogger(XlsxExporter.class);

    @Override
    public void export(JasperPrint print, OutputStream output) {
        log.debug("Exporting report '{}' to XLSX", print.getName());
        try {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setOnePagePerSheet(false);
            config.setDetectCellType(true);
            config.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(config);

            exporter.exportReport();
            log.info("Exported report '{}' to XLSX", print.getName());
        } catch (JRException e) {
            throw new ReportExportException("Failed to export report to XLSX: " + print.getName(), e);
        }
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.XLSX;
    }
}
