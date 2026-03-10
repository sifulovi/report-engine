package com.jasperframework.exporter;

import com.jasperframework.core.ExportFormat;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.OutputStream;

/**
 * Strategy interface for exporting a filled {@link JasperPrint} to a specific format.
 */
public interface ReportExporter {

    /**
     * Exports the filled report to the given output stream.
     *
     * @param print  the filled report
     * @param output destination stream (caller is responsible for closing)
     * @throws ReportExportException if export fails
     */
    void export(JasperPrint print, OutputStream output);

    /**
     * Returns the format this exporter handles.
     */
    ExportFormat getFormat();
}
