package com.jasperframework.registry;

/**
 * Defines a subreport relationship — the parameter name through which the compiled
 * subreport is injected into the parent, and the classpath location of the JRXML.
 *
 * <p>In the parent JRXML, the subreport element references the compiled subreport
 * via a parameter expression, e.g. {@code $P{ORDER_ITEMS_SUBREPORT}}.
 * The framework resolves and injects the compiled {@code JasperReport} object
 * automatically, avoiding hard-coded file paths.</p>
 */
public final class SubreportDefinition {

    private final String parameterName;
    private final String templatePath;

    /**
     * @param parameterName the parameter name used in the parent JRXML to reference this subreport
     * @param templatePath  classpath-relative path to the subreport JRXML
     */
    public SubreportDefinition(String parameterName, String templatePath) {
        if (parameterName == null || parameterName.isBlank()) {
            throw new IllegalArgumentException("parameterName must not be blank");
        }
        if (templatePath == null || templatePath.isBlank()) {
            throw new IllegalArgumentException("templatePath must not be blank");
        }
        this.parameterName = parameterName;
        this.templatePath = templatePath;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    @Override
    public String toString() {
        return "SubreportDefinition{param='" + parameterName + "', path='" + templatePath + "'}";
    }
}
