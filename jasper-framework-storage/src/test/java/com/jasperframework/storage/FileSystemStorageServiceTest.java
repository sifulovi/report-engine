package com.jasperframework.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileSystemStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new FileSystemStorageService(tempDir);
    }

    @Test
    void storeAndRetrieve() throws IOException {
        byte[] content = "hello report".getBytes(StandardCharsets.UTF_8);
        storage.store("test.pdf", content);

        assertThat(storage.exists("test.pdf")).isTrue();

        try (InputStream in = storage.retrieve("test.pdf")) {
            assertThat(in.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void storeOverwritesExisting() throws IOException {
        storage.store("report.pdf", "v1".getBytes(StandardCharsets.UTF_8));
        storage.store("report.pdf", "v2".getBytes(StandardCharsets.UTF_8));

        try (InputStream in = storage.retrieve("report.pdf")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("v2");
        }
    }

    @Test
    void storeCreatesIntermediateDirectories() throws IOException {
        byte[] content = "nested".getBytes(StandardCharsets.UTF_8);
        storage.store("invoices/2024/report.pdf", content);

        assertThat(storage.exists("invoices/2024/report.pdf")).isTrue();
        try (InputStream in = storage.retrieve("invoices/2024/report.pdf")) {
            assertThat(in.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void retrieveNonExistentKeyThrows() {
        assertThatThrownBy(() -> storage.retrieve("missing.pdf"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteRemovesFile() {
        storage.store("to-delete.pdf", "data".getBytes(StandardCharsets.UTF_8));
        assertThat(storage.exists("to-delete.pdf")).isTrue();

        storage.delete("to-delete.pdf");
        assertThat(storage.exists("to-delete.pdf")).isFalse();
    }

    @Test
    void deleteNonExistentKeyDoesNotThrow() {
        storage.delete("nonexistent.pdf");
    }

    @Test
    void existsReturnsFalseForMissingKey() {
        assertThat(storage.exists("nope.pdf")).isFalse();
    }

    @Test
    void pathTraversalIsRejected() {
        assertThatThrownBy(() -> storage.store("../../etc/passwd", new byte[0]))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("escapes root");
    }

    @Test
    void nullKeyThrows() {
        assertThatThrownBy(() -> storage.store(null, new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankKeyThrows() {
        assertThatThrownBy(() -> storage.store("  ", new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullContentThrows() {
        assertThatThrownBy(() -> storage.store("key.pdf", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullRootDirThrows() {
        assertThatThrownBy(() -> new FileSystemStorageService(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRootDirReturnsConfiguredPath() {
        assertThat(storage.getRootDir()).isEqualTo(tempDir.toAbsolutePath().normalize());
    }
}
