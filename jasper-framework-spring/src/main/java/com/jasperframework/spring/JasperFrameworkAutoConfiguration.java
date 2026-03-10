package com.jasperframework.spring;

import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.core.ReportExecutor;
import com.jasperframework.exporter.ExportService;
import com.jasperframework.registry.ReportRegistry;
import com.jasperframework.registry.SubreportResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot autoconfiguration for the Jasper Framework.
 * Registers core beans when they are not already provided by the application.
 */
@Configuration
public class JasperFrameworkAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "jasper.framework")
    @ConditionalOnMissingBean
    public JasperFrameworkProperties jasperFrameworkProperties() {
        return new JasperFrameworkProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReportCompiler reportCompiler() {
        return new ReportCompiler();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReportExecutor reportExecutor() {
        return new ReportExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReportEngine reportEngine(ReportCompiler compiler, ReportExecutor executor) {
        return new ReportEngine(compiler, executor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReportRegistry reportRegistry() {
        return new ReportRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public SubreportResolver subreportResolver(ReportCompiler compiler) {
        return new SubreportResolver(compiler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExportService exportService() {
        return new ExportService();
    }
}
