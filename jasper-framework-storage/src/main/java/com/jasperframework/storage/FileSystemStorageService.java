package com.jasperframework.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * {@link ReportStorageService} backed by the local file system.
 * <p>
 * Keys are resolved relative to a configurable root directory.
 * Intermediate directories are created automatically on {@link #store}.
 */
public class FileSystemStorageService implements ReportStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);

    private final Path rootDir;

    /**
     * @param rootDir the root directory for stored reports (created if absent)
     * @throws StorageException if the directory cannot be created
     */
    public FileSystemStorageService(Path rootDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException("rootDir must not be null");
        }
        try {
            this.rootDir = Files.createDirectories(rootDir).toAbsolutePath().normalize();
        } catch (IOException e) {
            throw new StorageException("Failed to create storage directory: " + rootDir, e);
        }
        log.info("FileSystemStorageService initialized at {}", this.rootDir);
    }

    @Override
    public void store(String key, byte[] content) {
        validateKey(key);
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }

        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Stored {} ({} bytes)", key, content.length);
        } catch (IOException e) {
            throw new StorageException("Failed to store key: " + key, e);
        }
    }

    @Override
    public InputStream retrieve(String key) {
        validateKey(key);
        Path target = resolve(key);
        if (!Files.exists(target)) {
            throw new StorageException("Key not found: " + key);
        }
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new StorageException("Failed to retrieve key: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        validateKey(key);
        Path target = resolve(key);
        try {
            Files.deleteIfExists(target);
            log.debug("Deleted {}", key);
        } catch (IOException e) {
            throw new StorageException("Failed to delete key: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        validateKey(key);
        return Files.exists(resolve(key));
    }

    /**
     * Returns the root directory used by this service.
     */
    public Path getRootDir() {
        return rootDir;
    }

    private Path resolve(String key) {
        Path resolved = rootDir.resolve(key).normalize();
        if (!resolved.startsWith(rootDir)) {
            throw new StorageException("Key escapes root directory: " + key);
        }
        return resolved;
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be null or blank");
        }
    }
}
