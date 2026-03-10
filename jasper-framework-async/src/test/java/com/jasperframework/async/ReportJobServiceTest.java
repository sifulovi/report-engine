package com.jasperframework.async;

import com.jasperframework.core.ExportFormat;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.core.ReportException;
import com.jasperframework.exporter.ExportService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class ReportJobServiceTest {

    private static ReportJobService jobService;

    @BeforeAll
    static void setUp() {
        ReportEngine engine = new ReportEngine();
        ExportService exportService = new ExportService();
        ReportWorker worker = new ReportWorker(engine, exportService);
        jobService = new ReportJobService(worker, 2);
    }

    @AfterAll
    static void tearDown() {
        jobService.close();
    }

    @Test
    void submit_shouldCreatePendingJob() {
        ReportJob job = jobService.submit("TEST", "reports/simple.jrxml",
                ExportFormat.PDF, Map.of("title", "Async Test"));

        assertThat(job.getId()).isNotBlank();
        assertThat(job.getReportCode()).isEqualTo("TEST");
        assertThat(job.getCreatedAt()).isNotNull();
    }

    @Test
    void submit_shouldCompleteAsynchronously() {
        ReportJob job = jobService.submit("TEST", "reports/simple.jrxml",
                ExportFormat.PDF, Map.of("title", "Async Test"));

        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> job.getStatus() == JobStatus.COMPLETED);

        assertThat(job.getResult()).isNotEmpty();
        assertThat(job.getCompletedAt()).isNotNull();
    }

    @Test
    void submit_shouldFailForInvalidTemplate() {
        ReportJob job = jobService.submit("BAD", "reports/nonexistent.jrxml",
                ExportFormat.PDF, Map.of());

        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> job.getStatus() == JobStatus.FAILED);

        assertThat(job.getErrorMessage()).isNotBlank();
    }

    @Test
    void getJob_shouldReturnSubmittedJob() {
        ReportJob job = jobService.submit("TEST", "reports/simple.jrxml",
                ExportFormat.CSV, Map.of("title", "Lookup Test"));

        ReportJob found = jobService.getJob(job.getId());
        assertThat(found).isSameAs(job);
    }

    @Test
    void getJob_shouldThrowForUnknownId() {
        assertThatThrownBy(() -> jobService.getJob("nonexistent"))
                .isInstanceOf(ReportException.class);
    }

    @Test
    void getAllJobs_shouldIncludeSubmittedJobs() {
        int before = jobService.getAllJobs().size();
        jobService.submit("TEST", "reports/simple.jrxml",
                ExportFormat.PDF, Map.of("title", "Count Test"));

        assertThat(jobService.getAllJobs()).hasSizeGreaterThan(before);
    }
}
