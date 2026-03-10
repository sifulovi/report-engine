package com.jasperframework.registry;

import com.jasperframework.core.ReportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe in-memory registry of report definitions.
 * <p>
 * Reports are registered by a unique code and can be looked up at runtime.
 *
 * <pre>{@code
 * ReportRegistry registry = new ReportRegistry();
 * registry.register(ReportDefinition.builder("INV-001", "reports/invoice.jrxml")
 *         .displayName("Invoice")
 *         .format(ExportFormat.PDF)
 *         .build());
 *
 * ReportDefinition def = registry.lookup("INV-001");
 * }</pre>
 */
public class ReportRegistry {

    private static final Logger log = LoggerFactory.getLogger(ReportRegistry.class);

    private final ConcurrentMap<String, ReportDefinition> definitions = new ConcurrentHashMap<>();

    /**
     * Registers a report definition. Overwrites any existing definition with the same code.
     */
    public void register(ReportDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        definitions.put(definition.getCode(), definition);
        log.info("Registered report: {} -> {}", definition.getCode(), definition.getTemplatePath());
    }

    /**
     * Looks up a report definition by code.
     *
     * @throws ReportException if no report is registered with the given code
     */
    public ReportDefinition lookup(String code) {
        ReportDefinition def = definitions.get(code);
        if (def == null) {
            throw new ReportException("No report registered with code: " + code);
        }
        return def;
    }

    /**
     * Returns the definition if registered, or {@code null} otherwise.
     */
    public ReportDefinition find(String code) {
        return definitions.get(code);
    }

    /**
     * Returns {@code true} if a report with the given code is registered.
     */
    public boolean contains(String code) {
        return definitions.containsKey(code);
    }

    /**
     * Removes a report definition by code.
     *
     * @return the removed definition, or {@code null} if not found
     */
    public ReportDefinition unregister(String code) {
        ReportDefinition removed = definitions.remove(code);
        if (removed != null) {
            log.info("Unregistered report: {}", code);
        }
        return removed;
    }

    /**
     * Returns an unmodifiable view of all registered definitions.
     */
    public Collection<ReportDefinition> getAll() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    /**
     * Returns the number of registered reports.
     */
    public int size() {
        return definitions.size();
    }

    /**
     * Removes all registered definitions.
     */
    public void clear() {
        definitions.clear();
        log.info("Registry cleared");
    }
}
