package com.jasperframework.async;

import com.jasperframework.core.ExportFormat;
import com.jasperframework.core.ReportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages asynchronous report generation jobs using {@code java.util.concurrent}.
 * <p>
 * Framework-agnostic — works in any Java environment. Spring applications can
 * wrap this with {@code @Async} support later.
 *
 * <pre>{@code
 * ReportJobService jobService = new ReportJobService(worker, 4);
 * ReportJob job = jobService.submit("INV-001", "reports/invoice.jrxml",
 *         ExportFormat.PDF, Map.of("id", 42));
 *
 * // Poll later
 * ReportJob completed = jobService.getJob(job.getId());
 * if (completed.getStatus() == JobStatus.COMPLETED) {
 *     byte[] pdf = completed.getResult();
 * }
 * }</pre>
 */
public class ReportJobService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ReportJobService.class);

    private final ReportWorker worker;
    private final ExecutorService executor;
    private final ConcurrentMap<String, ReportJob> jobs = new ConcurrentHashMap<>();

    public ReportJobService(ReportWorker worker) {
        this(worker, Runtime.getRuntime().availableProcessors());
    }

    public ReportJobService(ReportWorker worker, int threadPoolSize) {
        if (worker == null) {
            throw new IllegalArgumentException("worker must not be null");
        }
        this.worker = worker;
        this.executor = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "report-worker");
            t.setDaemon(true);
            return t;
        });
        log.info("ReportJobService initialized with {} threads", threadPoolSize);
    }

    /**
     * Submits a new report generation job.
     *
     * @return the created job (initially in PENDING state)
     */
    public ReportJob submit(String reportCode, String templatePath,
                            ExportFormat format, Map<String, Object> parameters) {
        ReportJob job = new ReportJob(reportCode, templatePath, format, parameters);
        jobs.put(job.getId(), job);
        log.info("Submitted job {} for report '{}'", job.getId(), reportCode);

        executor.submit(() -> worker.execute(job));
        return job;
    }

    /**
     * Returns the job by ID.
     *
     * @throws ReportException if the job is not found
     */
    public ReportJob getJob(String jobId) {
        ReportJob job = jobs.get(jobId);
        if (job == null) {
            throw new ReportException("Job not found: " + jobId);
        }
        return job;
    }

    /**
     * Returns all known jobs.
     */
    public Collection<ReportJob> getAllJobs() {
        return Collections.unmodifiableCollection(jobs.values());
    }

    /**
     * Shuts down the executor and waits for running jobs to complete.
     */
    @Override
    public void close() {
        log.info("Shutting down ReportJobService");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
