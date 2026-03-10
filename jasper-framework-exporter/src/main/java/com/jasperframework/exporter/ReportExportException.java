package com.jasperframework.exporter;

import com.jasperframework.core.ReportException;

/**
 * Thrown when report export fails.
 */
public class ReportExportException extends ReportException {

    public ReportExportException(String message) {
        super(message);
    }

    public ReportExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
