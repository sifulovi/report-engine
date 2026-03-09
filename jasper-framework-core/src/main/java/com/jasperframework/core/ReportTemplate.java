package com.jasperframework.core;

import net.sf.jasperreports.engine.JasperReport;

/**
 * Wraps a compiled JasperReport with its source location for cache management.
 */
public final class ReportTemplate {

    private final String templatePath;
    private final JasperReport compiledReport;

    public ReportTemplate(String templatePath, JasperReport compiledReport) {
        if (templatePath == null || templatePath.isBlank()) {
            throw new IllegalArgumentException("templatePath must not be blank");
        }
        if (compiledReport == null) {
            throw new IllegalArgumentException("compiledReport must not be null");
        }
        this.templatePath = templatePath;
        this.compiledReport = compiledReport;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public JasperReport getCompiledReport() {
        return compiledReport;
    }

    @Override
    public String toString() {
        return "ReportTemplate{path='" + templatePath + "'}";
    }
}
