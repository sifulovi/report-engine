package com.jasperframework.metadata;

/**
 * Metadata for a subreport relationship — maps to the {@code report_subreports} table.
 */
public class ReportSubreportMetadata {

    private String parameterName;
    private String templatePath;

    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }

    public String getTemplatePath() { return templatePath; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }
}
