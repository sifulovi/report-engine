package com.jasperframework.core;

/**
 * Thrown when a JRXML template cannot be found or compiled.
 */
public class ReportCompilationException extends ReportException {

    public ReportCompilationException(String message) {
        super(message);
    }

    public ReportCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
