package com.jasperframework.core;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe JRXML compiler with an in-memory cache.
 * <p>
 * Templates are loaded from the classpath and compiled once; subsequent
 * calls for the same path return the cached {@link JasperReport}.
 */
public class ReportCompiler {

    private static final Logger log = LoggerFactory.getLogger(ReportCompiler.class);

    private final ConcurrentMap<String, ReportTemplate> cache = new ConcurrentHashMap<>();

    /**
     * Compiles the JRXML at {@code templatePath} (classpath-relative) and caches the result.
     *
     * @param templatePath classpath resource path, e.g. {@code "reports/invoice.jrxml"}
     * @return compiled template wrapper
     * @throws ReportCompilationException if the resource is missing or compilation fails
     */
    public ReportTemplate compile(String templatePath) {
        return cache.computeIfAbsent(templatePath, this::doCompile);
    }

    /**
     * Returns a previously compiled template, or {@code null} if not cached.
     */
    public ReportTemplate getCached(String templatePath) {
        return cache.get(templatePath);
    }

    /**
     * Evicts a single template from the cache, forcing recompilation on next access.
     */
    public void evict(String templatePath) {
        cache.remove(templatePath);
        log.debug("Evicted cached template: {}", templatePath);
    }

    /**
     * Clears the entire template cache.
     */
    public void clearCache() {
        cache.clear();
        log.info("Template cache cleared");
    }

    /**
     * Returns the number of cached templates.
     */
    public int cacheSize() {
        return cache.size();
    }

    private ReportTemplate doCompile(String templatePath) {
        log.debug("Compiling JRXML template: {}", templatePath);

        InputStream stream = resolveTemplate(templatePath);
        try {
            JasperReport compiled = JasperCompileManager.compileReport(stream);
            log.info("Compiled and cached template: {}", templatePath);
            return new ReportTemplate(templatePath, compiled);
        } catch (JRException e) {
            throw new ReportCompilationException(
                    "Failed to compile JRXML template: " + templatePath, e);
        }
    }

    private InputStream resolveTemplate(String templatePath) {
        InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(templatePath);
        if (stream == null) {
            throw new ReportCompilationException(
                    "JRXML template not found on classpath: " + templatePath);
        }
        return stream;
    }
}
