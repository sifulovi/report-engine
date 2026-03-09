package com.jasperframework.core;

/**
 * Thrown when report filling (execution) fails.
 */
public class ReportExecutionException extends ReportException {

    public ReportExecutionException(String message) {
        super(message);
    }

    public ReportExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
