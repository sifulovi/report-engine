package com.jasperframework.metadata;

import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportContext;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.core.ReportException;
import com.jasperframework.core.ReportTemplate;
import com.jasperframework.registry.ReportDefinition;
import com.jasperframework.registry.ReportRegistry;
import com.jasperframework.registry.SubreportResolver;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrates metadata-driven report generation:
 * <ol>
 *   <li>Load metadata from the database</li>
 *   <li>Validate parameters</li>
 *   <li>Execute the SQL query</li>
 *   <li>Resolve subreports</li>
 *   <li>Fill the report</li>
 * </ol>
 *
 * <pre>{@code
 * ReportMetadataService service = new ReportMetadataService(
 *         dataSource, engine, registry, compiler);
 * JasperPrint print = service.generateFromMetadata("INV-001",
 *         Map.of("customerId", 42));
 * }</pre>
 */
public class ReportMetadataService {

    private static final Logger log = LoggerFactory.getLogger(ReportMetadataService.class);

    private final ReportMetadataLoader metadataLoader;
    private final QueryExecutor queryExecutor;
    private final ReportEngine engine;
    private final SubreportResolver subreportResolver;

    public ReportMetadataService(DataSource dataSource, ReportEngine engine,
                                 ReportCompiler compiler) {
        this.metadataLoader = new ReportMetadataLoader(dataSource);
        this.queryExecutor = new QueryExecutor(dataSource);
        this.engine = engine;
        this.subreportResolver = new SubreportResolver(compiler);
    }

    public ReportMetadataService(ReportMetadataLoader metadataLoader, QueryExecutor queryExecutor,
                                 ReportEngine engine, SubreportResolver subreportResolver) {
        this.metadataLoader = metadataLoader;
        this.queryExecutor = queryExecutor;
        this.engine = engine;
        this.subreportResolver = subreportResolver;
    }

    /**
     * Generates a report from database metadata.
     *
     * @param reportCode the report code in the metadata table
     * @param params     user-supplied parameters
     * @return filled JasperPrint ready for export
     */
    public JasperPrint generateFromMetadata(String reportCode, Map<String, Object> params) {
        log.debug("Generating report from metadata: {}", reportCode);

        // 1. Load metadata
        ReportMetadata metadata = metadataLoader.load(reportCode);
        if (!metadata.isActive()) {
            throw new ReportException("Report is inactive: " + reportCode);
        }

        // 2. Validate required parameters
        validateParameters(metadata, params);

        // 3. Build parameters map (user params + defaults + subreports)
        Map<String, Object> allParams = new HashMap<>();
        applyDefaults(metadata, allParams);
        allParams.putAll(params);

        // 4. Resolve subreports if any
        if (!metadata.getSubreports().isEmpty()) {
            ReportDefinition def = toDefinition(metadata);
            Map<String, Object> subreportParams = subreportResolver.resolveSubreports(def);
            allParams.putAll(subreportParams);
        }

        // 5. Execute query and get data source (if query is defined)
        ReportContext.Builder contextBuilder = ReportContext.builder(metadata.getTemplatePath())
                .parameters(allParams);

        if (metadata.getQuery() != null && !metadata.getQuery().isBlank()) {
            JRDataSource dataSource = queryExecutor.execute(metadata.getQuery(), allParams);
            contextBuilder.dataSource(dataSource);
        }

        // 6. Fill report
        JasperPrint print = engine.generateReport(contextBuilder.build());
        log.info("Generated report from metadata: {}", reportCode);
        return print;
    }

    private void validateParameters(ReportMetadata metadata, Map<String, Object> params) {
        for (ReportParameterMetadata param : metadata.getParameters()) {
            if (param.isRequired() && !params.containsKey(param.getName())) {
                if (param.getDefaultValue() == null) {
                    throw new ReportException(
                            "Required parameter missing: " + param.getName() +
                                    " for report: " + metadata.getCode());
                }
            }
        }
    }

    private void applyDefaults(ReportMetadata metadata, Map<String, Object> params) {
        for (ReportParameterMetadata param : metadata.getParameters()) {
            if (param.getDefaultValue() != null && !params.containsKey(param.getName())) {
                params.put(param.getName(), convertDefault(param));
            }
        }
    }

    private Object convertDefault(ReportParameterMetadata param) {
        String value = param.getDefaultValue();
        if (value == null) return null;
        return switch (param.getType()) {
            case "java.lang.Integer" -> Integer.valueOf(value);
            case "java.lang.Long" -> Long.valueOf(value);
            case "java.lang.Double" -> Double.valueOf(value);
            case "java.lang.Boolean" -> Boolean.valueOf(value);
            default -> value;
        };
    }

    private ReportDefinition toDefinition(ReportMetadata metadata) {
        ReportDefinition.Builder builder = ReportDefinition.builder(
                metadata.getCode(), metadata.getTemplatePath());
        for (ReportSubreportMetadata sub : metadata.getSubreports()) {
            builder.subreport(sub.getParameterName(), sub.getTemplatePath());
        }
        return builder.build();
    }
}
