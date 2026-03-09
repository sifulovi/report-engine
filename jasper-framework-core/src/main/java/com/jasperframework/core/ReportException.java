package com.jasperframework.core;

/**
 * Base unchecked exception for all report framework errors.
 */
public class ReportException extends RuntimeException {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
