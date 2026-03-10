package com.jasperframework.spring;

/**
 * Configuration properties for the Jasper Framework Spring Boot integration.
 * Maps to {@code jasper.framework.*} in application.yml/properties.
 */
public class JasperFrameworkProperties {

    private String templatePrefix = "reports/";
    private boolean cacheEnabled = true;
    private int asyncThreadPoolSize = Runtime.getRuntime().availableProcessors();

    public String getTemplatePrefix() { return templatePrefix; }
    public void setTemplatePrefix(String templatePrefix) { this.templatePrefix = templatePrefix; }

    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }

    public int getAsyncThreadPoolSize() { return asyncThreadPoolSize; }
    public void setAsyncThreadPoolSize(int asyncThreadPoolSize) { this.asyncThreadPoolSize = asyncThreadPoolSize; }
}
