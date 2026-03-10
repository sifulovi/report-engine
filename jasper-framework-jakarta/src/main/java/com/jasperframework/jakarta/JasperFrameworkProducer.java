package com.jasperframework.jakarta;

import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.core.ReportExecutor;
import com.jasperframework.exporter.ExportService;
import com.jasperframework.registry.ReportRegistry;
import com.jasperframework.registry.SubreportResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * CDI producer for Jasper Framework beans in Jakarta EE environments.
 * <p>
 * Register this class in your {@code beans.xml} or let CDI discover it via bean scanning.
 *
 * <pre>{@code
 * // In a JSF backing bean or EJB:
 * @Inject
 * private ReportEngine reportEngine;
 *
 * @Inject
 * private ExportService exportService;
 * }</pre>
 */
@ApplicationScoped
public class JasperFrameworkProducer {

    @Produces
    @ApplicationScoped
    public ReportCompiler reportCompiler() {
        return new ReportCompiler();
    }

    @Produces
    @ApplicationScoped
    public ReportExecutor reportExecutor() {
        return new ReportExecutor();
    }

    @Produces
    @ApplicationScoped
    public ReportEngine reportEngine() {
        return new ReportEngine(reportCompiler(), reportExecutor());
    }

    @Produces
    @ApplicationScoped
    public ReportRegistry reportRegistry() {
        return new ReportRegistry();
    }

    @Produces
    @ApplicationScoped
    public SubreportResolver subreportResolver() {
        return new SubreportResolver(reportCompiler());
    }

    @Produces
    @ApplicationScoped
    public ExportService exportService() {
        return new ExportService();
    }
}
