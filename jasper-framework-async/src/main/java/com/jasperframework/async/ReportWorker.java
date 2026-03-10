package com.jasperframework.async;

import com.jasperframework.core.ReportContext;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.exporter.ExportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a single report job: compiles, fills, and exports.
 * Each invocation of {@link #execute(ReportJob)} is self-contained and thread-safe.
 */
public class ReportWorker {

    private static final Logger log = LoggerFactory.getLogger(ReportWorker.class);

    private final ReportEngine engine;
    private final ExportService exportService;

    public ReportWorker(ReportEngine engine, ExportService exportService) {
        this.engine = engine;
        this.exportService = exportService;
    }

    /**
     * Executes the job: compiles, fills, exports, and updates the job status.
     */
    public void execute(ReportJob job) {
        job.markRunning();
        log.debug("Executing job {} for report '{}'", job.getId(), job.getReportCode());

        try {
            ReportContext context = ReportContext.builder(job.getTemplatePath())
                    .parameters(job.getParameters())
                    .build();

            JasperPrint print = engine.generateReport(context);
            byte[] exported = exportService.exportToBytes(print, job.getFormat());

            job.markCompleted(exported);
            log.info("Job {} completed: {} bytes", job.getId(), exported.length);
        } catch (Exception e) {
            job.markFailed(e.getMessage());
            log.error("Job {} failed: {}", job.getId(), e.getMessage(), e);
        }
    }
}
