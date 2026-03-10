package com.jasperframework.composition;

import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Merges multiple filled {@link JasperPrint} instances into a single composite report
 * by combining their pages. The resulting merged report can then be exported as one document.
 *
 * <pre>{@code
 * ReportComposer composer = new ReportComposer();
 * JasperPrint merged = composer.compose("Financial Report",
 *         balanceSheet, incomeStatement, cashFlow);
 * }</pre>
 */
public class ReportComposer {

    private static final Logger log = LoggerFactory.getLogger(ReportComposer.class);

    /**
     * Merges multiple filled reports into a single report by appending pages.
     *
     * @param name    the name for the composite report
     * @param reports the filled reports to merge (order is preserved)
     * @return a single JasperPrint containing all pages from the input reports
     * @throws IllegalArgumentException if no reports are provided
     */
    public JasperPrint compose(String name, JasperPrint... reports) {
        return compose(name, List.of(reports));
    }

    /**
     * Merges multiple filled reports into a single report by appending pages.
     *
     * @param name    the name for the composite report
     * @param reports the filled reports to merge (order is preserved)
     * @return a single JasperPrint containing all pages from the input reports
     * @throws IllegalArgumentException if the list is null or empty
     */
    public JasperPrint compose(String name, List<JasperPrint> reports) {
        if (reports == null || reports.isEmpty()) {
            throw new IllegalArgumentException("At least one report is required for composition");
        }

        log.debug("Composing {} reports into '{}'", reports.size(), name);

        // Use the first report as the base (inherits page format)
        JasperPrint base = reports.get(0);
        JasperPrint merged = new JasperPrint();
        merged.setName(name);
        merged.setPageWidth(base.getPageWidth());
        merged.setPageHeight(base.getPageHeight());

        int totalPages = 0;
        for (JasperPrint report : reports) {
            int pageCount = report.getPages().size();
            log.debug("Appending {} pages from report '{}'", pageCount, report.getName());
            for (int i = 0; i < pageCount; i++) {
                merged.addPage(report.getPages().get(i));
            }
            totalPages += pageCount;
        }

        log.info("Composed '{}': {} reports -> {} total pages", name, reports.size(), totalPages);
        return merged;
    }
}
