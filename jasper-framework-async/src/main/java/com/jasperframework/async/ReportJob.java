package com.jasperframework.async;

import com.jasperframework.core.ExportFormat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an asynchronous report generation job.
 */
public class ReportJob {

    private final String id;
    private final String reportCode;
    private final String templatePath;
    private final ExportFormat format;
    private final Map<String, Object> parameters;
    private volatile JobStatus status;
    private volatile byte[] result;
    private volatile String errorMessage;
    private final Instant createdAt;
    private volatile Instant completedAt;

    public ReportJob(String reportCode, String templatePath, ExportFormat format,
                     Map<String, Object> parameters) {
        this.id = UUID.randomUUID().toString();
        this.reportCode = reportCode;
        this.templatePath = templatePath;
        this.format = format;
        this.parameters = parameters;
        this.status = JobStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getReportCode() { return reportCode; }
    public String getTemplatePath() { return templatePath; }
    public ExportFormat getFormat() { return format; }
    public Map<String, Object> getParameters() { return parameters; }
    public JobStatus getStatus() { return status; }
    public byte[] getResult() { return result; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }

    void markRunning() {
        this.status = JobStatus.RUNNING;
    }

    void markCompleted(byte[] result) {
        this.result = result;
        this.status = JobStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    void markFailed(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = JobStatus.FAILED;
        this.completedAt = Instant.now();
    }
}
