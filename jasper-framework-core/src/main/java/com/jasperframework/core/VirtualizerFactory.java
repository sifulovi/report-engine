package com.jasperframework.core;

import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates {@link JRVirtualizer} instances from a {@link VirtualizerConfig}.
 *
 * <pre>{@code
 * VirtualizerConfig config = VirtualizerConfig.file(20);
 * JRVirtualizer virtualizer = VirtualizerFactory.create(config);
 * }</pre>
 */
public final class VirtualizerFactory {

    private static final Logger log = LoggerFactory.getLogger(VirtualizerFactory.class);

    private VirtualizerFactory() { }

    /**
     * Creates a new virtualizer for the given configuration.
     *
     * @param config virtualizer configuration
     * @return a new virtualizer instance
     */
    public static JRVirtualizer create(VirtualizerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        return switch (config.getType()) {
            case FILE -> createFileVirtualizer(config);
            case SWAP_FILE -> createSwapFileVirtualizer(config);
            case GZIP -> createGzipVirtualizer(config);
        };
    }

    private static JRVirtualizer createFileVirtualizer(VirtualizerConfig config) {
        String dir = config.getSwapDirectory().toAbsolutePath().toString();
        log.debug("Creating JRFileVirtualizer: maxPages={}, dir={}",
                config.getMaxPagesInMemory(), dir);
        return new JRFileVirtualizer(config.getMaxPagesInMemory(), dir);
    }

    private static JRVirtualizer createSwapFileVirtualizer(VirtualizerConfig config) {
        String dir = config.getSwapDirectory().toAbsolutePath().toString();
        log.debug("Creating JRSwapFileVirtualizer: maxPages={}, dir={}, blockSize={}",
                config.getMaxPagesInMemory(), dir, config.getBlockSize());
        JRSwapFile swapFile = new JRSwapFile(dir, config.getBlockSize(), config.getBlockSize());
        return new JRSwapFileVirtualizer(config.getMaxPagesInMemory(), swapFile, true);
    }

    private static JRVirtualizer createGzipVirtualizer(VirtualizerConfig config) {
        log.debug("Creating JRGzipVirtualizer: maxPages={}", config.getMaxPagesInMemory());
        return new JRGzipVirtualizer(config.getMaxPagesInMemory());
    }
}
