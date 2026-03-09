package com.jasperframework.core;

import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the report framework.
 * <p>
 * Orchestrates compilation (with caching) and execution in a single call.
 * Thread-safe — a single instance can be shared across the application.
 *
 * <pre>{@code
 * ReportEngine engine = new ReportEngine();
 *
 * ReportContext ctx = ReportContext.builder("reports/invoice.jrxml")
 *         .parameter("invoiceId", 42)
 *         .connection(dataSource.getConnection())
 *         .build();
 *
 * JasperPrint print = engine.generateReport(ctx);
 * }</pre>
 */
public class ReportEngine {

    private static final Logger log = LoggerFactory.getLogger(ReportEngine.class);

    private final ReportCompiler compiler;
    private final ReportExecutor executor;

    public ReportEngine() {
        this(new ReportCompiler(), new ReportExecutor());
    }

    public ReportEngine(ReportCompiler compiler, ReportExecutor executor) {
        if (compiler == null) {
            throw new IllegalArgumentException("compiler must not be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }
        this.compiler = compiler;
        this.executor = executor;
    }

    /**
     * Compiles (or retrieves from cache) and fills the report described by the context.
     *
     * @param context report context with template path, parameters, and data source
     * @return filled {@link JasperPrint} ready for export
     */
    public JasperPrint generateReport(ReportContext context) {
        log.debug("Generating report for template: {}", context.getTemplatePath());
        ReportTemplate template = compiler.compile(context.getTemplatePath());
        return executor.execute(template, context);
    }

    /**
     * Returns the underlying compiler for direct cache management.
     */
    public ReportCompiler getCompiler() {
        return compiler;
    }

    /**
     * Returns the underlying executor.
     */
    public ReportExecutor getExecutor() {
        return executor;
    }
}
