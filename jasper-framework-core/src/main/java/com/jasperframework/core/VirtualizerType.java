package com.jasperframework.core;

/**
 * Supported JasperReports virtualizer strategies for large report generation.
 * <p>
 * Virtualizers swap report pages to an external store when memory is tight,
 * allowing reports with thousands of pages to be generated without
 * running out of heap space.
 *
 * <ul>
 *   <li>{@link #FILE} — swaps each page to an individual temporary file; simplest option.</li>
 *   <li>{@link #SWAP_FILE} — uses a single swap file with block allocation; best I/O performance.</li>
 *   <li>{@link #GZIP} — compresses pages in memory; no disk I/O, but still limited by heap.</li>
 * </ul>
 */
public enum VirtualizerType {
    FILE,
    SWAP_FILE,
    GZIP
}
