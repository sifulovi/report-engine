package com.jasperframework.exporter;

import com.jasperframework.core.ExportFormat;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * Exports a filled report to CSV format.
 */
public class CsvExporter implements ReportExporter {

    private static final Logger log = LoggerFactory.getLogger(CsvExporter.class);

    @Override
    public void export(JasperPrint print, OutputStream output) {
        log.debug("Exporting report '{}' to CSV", print.getName());
        try {
            JRCsvExporter exporter = new JRCsvExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(output));

            SimpleCsvExporterConfiguration config = new SimpleCsvExporterConfiguration();
            config.setFieldDelimiter(",");
            exporter.setConfiguration(config);

            exporter.exportReport();
            log.info("Exported report '{}' to CSV", print.getName());
        } catch (JRException e) {
            throw new ReportExportException("Failed to export report to CSV: " + print.getName(), e);
        }
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.CSV;
    }
}
