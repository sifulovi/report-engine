package com.jasperframework.spring;

import com.jasperframework.async.ReportJobService;
import com.jasperframework.async.ReportWorker;
import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.core.ReportExecutor;
import com.jasperframework.exporter.ExportService;
import com.jasperframework.registry.ReportRegistry;
import com.jasperframework.registry.SubreportResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

    /**
     * Async beans — only created when the async module is on the classpath.
     */
    @Configuration
    @ConditionalOnClass(name = "com.jasperframework.async.ReportJobService")
    static class AsyncAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ReportWorker reportWorker(ReportEngine engine, ExportService exportService) {
            return new ReportWorker(engine, exportService);
        }

        @Bean(destroyMethod = "close")
        @ConditionalOnMissingBean
        public ReportJobService reportJobService(ReportWorker worker,
                                                  JasperFrameworkProperties properties) {
            return new ReportJobService(worker, properties.getAsyncThreadPoolSize());
        }
    }

    /**
     * REST controller — only created when Spring Web is on the classpath.
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    static class WebAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ReportRestController reportRestController(
                ReportEngine engine,
                ExportService exportService,
                JasperFrameworkProperties properties,
                org.springframework.beans.factory.ObjectProvider<ReportJobService> jobServiceProvider) {
            return new ReportRestController(engine, exportService, properties,
                    jobServiceProvider.getIfAvailable());
        }
    }
}
