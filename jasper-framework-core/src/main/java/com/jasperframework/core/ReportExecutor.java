package com.jasperframework.core;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe report executor — fills a compiled {@link JasperReport} with
 * parameters and a data source (or JDBC connection) to produce a {@link JasperPrint}.
 */
public class ReportExecutor {

    private static final Logger log = LoggerFactory.getLogger(ReportExecutor.class);

    /**
     * Fills the compiled report using the given context.
     *
     * @param template compiled report template
     * @param context  report context with parameters and data source / connection
     * @return filled report ready for export
     * @throws ReportExecutionException if filling fails
     */
    public JasperPrint execute(ReportTemplate template, ReportContext context) {
        log.debug("Filling report: {}", template.getTemplatePath());

        JasperReport report = template.getCompiledReport();
        Map<String, Object> params = new HashMap<>(context.getParameters());

        try {
            JasperPrint print;
            if (context.hasConnection()) {
                print = JasperFillManager.fillReport(report, params, context.getConnection());
            } else if (context.hasDataSource()) {
                print = JasperFillManager.fillReport(report, params, context.getDataSource());
            } else {
                // Empty data source — useful for static/parameter-only reports
                print = JasperFillManager.fillReport(report, params);
            }
            log.info("Report filled successfully: {} ({} pages)",
                    template.getTemplatePath(), print.getPages().size());
            return print;
        } catch (JRException e) {
            throw new ReportExecutionException(
                    "Failed to fill report: " + template.getTemplatePath(), e);
        }
    }
}
