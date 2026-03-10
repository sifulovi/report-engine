package com.jasperframework.registry;

import com.jasperframework.core.ReportCompiler;
import com.jasperframework.core.ReportTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves subreport definitions into compiled {@code JasperReport} objects
 * and injects them as parameters into the parent report's parameter map.
 * <p>
 * This eliminates file-path coupling between parent and subreport templates.
 * The parent JRXML references subreports via parameter expressions
 * (e.g. {@code $P{ORDER_ITEMS_SUBREPORT}}), and this resolver compiles and
 * injects the actual {@code JasperReport} objects before the parent is filled.
 *
 * <pre>{@code
 * SubreportResolver resolver = new SubreportResolver(compiler);
 * Map<String, Object> params = resolver.resolveSubreports(reportDefinition);
 * // params now contains compiled JasperReport objects keyed by parameter name
 * }</pre>
 */
public class SubreportResolver {

    private static final Logger log = LoggerFactory.getLogger(SubreportResolver.class);

    private final ReportCompiler compiler;

    public SubreportResolver(ReportCompiler compiler) {
        if (compiler == null) {
            throw new IllegalArgumentException("compiler must not be null");
        }
        this.compiler = compiler;
    }

    /**
     * Compiles all subreports defined in the report definition and returns a map
     * of parameter name to compiled {@code JasperReport}.
     *
     * @param definition the parent report definition containing subreport references
     * @return map of subreport parameters (empty if no subreports defined)
     */
    public Map<String, Object> resolveSubreports(ReportDefinition definition) {
        if (!definition.hasSubreports()) {
            return Map.of();
        }

        Map<String, Object> subreportParams = new HashMap<>();
        for (SubreportDefinition sub : definition.getSubreports()) {
            log.debug("Resolving subreport '{}' from '{}'", sub.getParameterName(), sub.getTemplatePath());
            ReportTemplate template = compiler.compile(sub.getTemplatePath());
            subreportParams.put(sub.getParameterName(), template.getCompiledReport());
            log.info("Resolved subreport: {} -> {}", sub.getParameterName(), sub.getTemplatePath());
        }
        return subreportParams;
    }
}
