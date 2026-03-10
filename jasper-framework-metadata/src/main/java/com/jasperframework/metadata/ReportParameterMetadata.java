package com.jasperframework.metadata;

/**
 * Metadata for a report parameter — maps to the {@code report_parameters} table.
 */
public class ReportParameterMetadata {

    private String name;
    private String type;
    private boolean required;
    private String defaultValue;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
}
