package com.jasperframework.storage;

import java.io.InputStream;

/**
 * Abstraction for storing and retrieving generated report files.
 * <p>
 * Implementations may back onto the local file system, S3, MinIO, or any
 * other binary store. Keys are simple strings — typically the job ID or a
 * path-like identifier such as {@code "invoices/2024/INV-001.pdf"}.
 *
 * <pre>{@code
 * ReportStorageService storage = new FileSystemStorageService(Paths.get("/reports"));
 * storage.store("job-123.pdf", pdfBytes);
 *
 * try (InputStream in = storage.retrieve("job-123.pdf")) {
 *     // stream to client
 * }
 * }</pre>
 */
public interface ReportStorageService {

    /**
     * Stores the given content under the specified key.
     * If a file already exists at the key it is overwritten.
     *
     * @param key     storage key (must not be null or blank)
     * @param content report bytes (must not be null)
     * @throws StorageException if the write fails
     */
    void store(String key, byte[] content);

    /**
     * Retrieves the content for the given key as an {@link InputStream}.
     * The caller is responsible for closing the stream.
     *
     * @param key storage key
     * @return input stream over the stored content
     * @throws StorageException if the key does not exist or reading fails
     */
    InputStream retrieve(String key);

    /**
     * Deletes the content associated with the key.
     * Does nothing if the key does not exist.
     *
     * @param key storage key
     * @throws StorageException if deletion fails
     */
    void delete(String key);

    /**
     * Checks whether content exists for the given key.
     *
     * @param key storage key
     * @return {@code true} if the key exists
     */
    boolean exists(String key);
}
