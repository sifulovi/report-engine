package com.jasperframework.spring;

import com.jasperframework.async.ReportJob;
import com.jasperframework.async.ReportJobService;
import com.jasperframework.core.ExportFormat;
import com.jasperframework.core.ReportContext;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.exporter.ExportService;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auto-registered REST controller for report generation.
 * <p>
 * Activated only when Spring Web and the required beans are present.
 *
 * <pre>
 * POST /reports/{code}?format=PDF    — generate report synchronously
 * GET  /reports/job/{id}              — check async job status
 * </pre>
 */
@RestController
@RequestMapping("/reports")
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
public class ReportRestController {

    private final ReportEngine engine;
    private final ExportService exportService;
    private final JasperFrameworkProperties properties;
    private final ReportJobService jobService;

    public ReportRestController(ReportEngine engine, ExportService exportService,
                                JasperFrameworkProperties properties,
                                ReportJobService jobService) {
        this.engine = engine;
        this.exportService = exportService;
        this.properties = properties;
        this.jobService = jobService;
    }

    /**
     * Generates a report synchronously and returns the exported bytes.
     *
     * @param code       report code / template name (resolved under the configured prefix)
     * @param format     export format (PDF, XLSX, CSV); defaults to PDF
     * @param parameters report parameters passed as query parameters
     */
    @PostMapping("/{code}")
    public ResponseEntity<byte[]> generate(
            @PathVariable String code,
            @RequestParam(defaultValue = "PDF") ExportFormat format,
            @RequestParam Map<String, Object> parameters) {

        // Remove the "format" key so it doesn't leak into report parameters
        parameters.remove("format");

        String templatePath = properties.getTemplatePrefix() + code + ".jrxml";

        ReportContext context = ReportContext.builder(templatePath)
                .parameters(parameters)
                .build();

        JasperPrint print = engine.generateReport(context);
        byte[] bytes = exportService.exportToBytes(print, format);

        String extension = format.name().toLowerCase();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + code + "." + extension + "\"")
                .contentType(mediaTypeFor(format))
                .body(bytes);
    }

    /**
     * Returns the status of an async report job.
     */
    @GetMapping("/job/{id}")
    public ResponseEntity<JobStatusResponse> jobStatus(@PathVariable String id) {
        ReportJob job = jobService.getJob(id);
        JobStatusResponse response = new JobStatusResponse(
                job.getId(),
                job.getReportCode(),
                job.getStatus().name(),
                job.getErrorMessage()
        );
        return ResponseEntity.ok(response);
    }

    private static MediaType mediaTypeFor(ExportFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case XLSX -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV -> MediaType.parseMediaType("text/csv");
        };
    }

    /**
     * JSON response for job status queries.
     */
    public record JobStatusResponse(String id, String reportCode, String status,
                                    String errorMessage) {}
}
