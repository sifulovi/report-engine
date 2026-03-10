package com.jasperframework.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Database-loaded report metadata — maps to the {@code reports} table.
 */
public class ReportMetadata {

    private String code;
    private String name;
    private String description;
    private String templatePath;
    private String query;
    private boolean active;
    private List<ReportParameterMetadata> parameters = new ArrayList<>();
    private List<ReportSubreportMetadata> subreports = new ArrayList<>();

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTemplatePath() { return templatePath; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<ReportParameterMetadata> getParameters() { return parameters; }
    public void setParameters(List<ReportParameterMetadata> parameters) {
        this.parameters = parameters != null ? parameters : new ArrayList<>();
    }

    public List<ReportSubreportMetadata> getSubreports() { return subreports; }
    public void setSubreports(List<ReportSubreportMetadata> subreports) {
        this.subreports = subreports != null ? subreports : new ArrayList<>();
    }
}
