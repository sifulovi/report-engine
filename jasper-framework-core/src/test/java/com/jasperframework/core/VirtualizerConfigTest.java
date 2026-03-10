package com.jasperframework.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VirtualizerConfigTest {

    @Test
    void fileWithDefaults() {
        VirtualizerConfig config = VirtualizerConfig.file(20);
        assertThat(config.getType()).isEqualTo(VirtualizerType.FILE);
        assertThat(config.getMaxPagesInMemory()).isEqualTo(20);
        assertThat(config.getSwapDirectory()).isNotNull();
    }

    @Test
    void fileWithCustomDirectory() {
        Path dir = Path.of("/tmp/reports");
        VirtualizerConfig config = VirtualizerConfig.file(10, dir);
        assertThat(config.getType()).isEqualTo(VirtualizerType.FILE);
        assertThat(config.getMaxPagesInMemory()).isEqualTo(10);
        assertThat(config.getSwapDirectory()).isEqualTo(dir);
    }

    @Test
    void swapFileWithDefaults() {
        VirtualizerConfig config = VirtualizerConfig.swapFile(30);
        assertThat(config.getType()).isEqualTo(VirtualizerType.SWAP_FILE);
        assertThat(config.getMaxPagesInMemory()).isEqualTo(30);
        assertThat(config.getBlockSize()).isEqualTo(2048);
    }

    @Test
    void swapFileWithCustomSettings() {
        Path dir = Path.of("/tmp/swap");
        VirtualizerConfig config = VirtualizerConfig.swapFile(15, dir, 4096);
        assertThat(config.getType()).isEqualTo(VirtualizerType.SWAP_FILE);
        assertThat(config.getMaxPagesInMemory()).isEqualTo(15);
        assertThat(config.getSwapDirectory()).isEqualTo(dir);
        assertThat(config.getBlockSize()).isEqualTo(4096);
    }

    @Test
    void gzip() {
        VirtualizerConfig config = VirtualizerConfig.gzip(50);
        assertThat(config.getType()).isEqualTo(VirtualizerType.GZIP);
        assertThat(config.getMaxPagesInMemory()).isEqualTo(50);
    }

    @Test
    void invalidMaxPagesThrows() {
        assertThatThrownBy(() -> VirtualizerConfig.file(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullSwapDirectoryThrows() {
        assertThatThrownBy(() -> VirtualizerConfig.file(10, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidBlockSizeThrows() {
        assertThatThrownBy(() -> VirtualizerConfig.swapFile(10, Path.of("/tmp"), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
