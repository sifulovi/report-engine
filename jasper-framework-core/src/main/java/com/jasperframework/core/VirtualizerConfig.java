package com.jasperframework.core;

import java.nio.file.Path;

/**
 * Configuration for a JasperReports virtualizer.
 * <p>
 * Use the static factory methods to create configurations for each strategy:
 *
 * <pre>{@code
 * // Keep at most 20 pages in memory, swap the rest to temp files
 * VirtualizerConfig config = VirtualizerConfig.file(20);
 *
 * // Single swap file with 4 KB blocks in a custom directory
 * VirtualizerConfig config = VirtualizerConfig.swapFile(20, Paths.get("/tmp/reports"), 4096);
 *
 * // In-memory GZIP compression
 * VirtualizerConfig config = VirtualizerConfig.gzip(50);
 * }</pre>
 */
public final class VirtualizerConfig {

    private final VirtualizerType type;
    private final int maxPagesInMemory;
    private final Path swapDirectory;
    private final int blockSize;

    private VirtualizerConfig(VirtualizerType type, int maxPagesInMemory,
                              Path swapDirectory, int blockSize) {
        if (maxPagesInMemory < 1) {
            throw new IllegalArgumentException("maxPagesInMemory must be >= 1");
        }
        this.type = type;
        this.maxPagesInMemory = maxPagesInMemory;
        this.swapDirectory = swapDirectory;
        this.blockSize = blockSize;
    }

    /**
     * Creates a file-based virtualizer config.
     *
     * @param maxPagesInMemory maximum pages to keep in memory before swapping to disk
     */
    public static VirtualizerConfig file(int maxPagesInMemory) {
        return new VirtualizerConfig(VirtualizerType.FILE, maxPagesInMemory,
                Path.of(System.getProperty("java.io.tmpdir")), 0);
    }

    /**
     * Creates a file-based virtualizer config with a custom swap directory.
     *
     * @param maxPagesInMemory maximum pages to keep in memory
     * @param swapDirectory    directory for temporary page files
     */
    public static VirtualizerConfig file(int maxPagesInMemory, Path swapDirectory) {
        if (swapDirectory == null) {
            throw new IllegalArgumentException("swapDirectory must not be null");
        }
        return new VirtualizerConfig(VirtualizerType.FILE, maxPagesInMemory, swapDirectory, 0);
    }

    /**
     * Creates a swap-file virtualizer config with default block size (2048 bytes).
     *
     * @param maxPagesInMemory maximum pages to keep in memory
     */
    public static VirtualizerConfig swapFile(int maxPagesInMemory) {
        return new VirtualizerConfig(VirtualizerType.SWAP_FILE, maxPagesInMemory,
                Path.of(System.getProperty("java.io.tmpdir")), 2048);
    }

    /**
     * Creates a swap-file virtualizer config.
     *
     * @param maxPagesInMemory maximum pages to keep in memory
     * @param swapDirectory    directory for the swap file
     * @param blockSize        block size in bytes (e.g. 2048 or 4096)
     */
    public static VirtualizerConfig swapFile(int maxPagesInMemory, Path swapDirectory, int blockSize) {
        if (swapDirectory == null) {
            throw new IllegalArgumentException("swapDirectory must not be null");
        }
        if (blockSize < 1) {
            throw new IllegalArgumentException("blockSize must be >= 1");
        }
        return new VirtualizerConfig(VirtualizerType.SWAP_FILE, maxPagesInMemory, swapDirectory, blockSize);
    }

    /**
     * Creates a GZIP in-memory virtualizer config.
     *
     * @param maxPagesInMemory maximum pages to keep uncompressed in memory
     */
    public static VirtualizerConfig gzip(int maxPagesInMemory) {
        return new VirtualizerConfig(VirtualizerType.GZIP, maxPagesInMemory, null, 0);
    }

    public VirtualizerType getType() { return type; }
    public int getMaxPagesInMemory() { return maxPagesInMemory; }
    public Path getSwapDirectory() { return swapDirectory; }
    public int getBlockSize() { return blockSize; }
}
