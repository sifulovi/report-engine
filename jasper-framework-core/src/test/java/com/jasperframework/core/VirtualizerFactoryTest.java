package com.jasperframework.core;

import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VirtualizerFactoryTest {

    @Test
    void createsFileVirtualizer() {
        VirtualizerConfig config = VirtualizerConfig.file(20);
        JRVirtualizer virtualizer = VirtualizerFactory.create(config);
        assertThat(virtualizer).isInstanceOf(JRFileVirtualizer.class);
    }

    @Test
    void createsSwapFileVirtualizer() {
        VirtualizerConfig config = VirtualizerConfig.swapFile(20, Path.of(System.getProperty("java.io.tmpdir")), 2048);
        JRVirtualizer virtualizer = VirtualizerFactory.create(config);
        assertThat(virtualizer).isInstanceOf(JRSwapFileVirtualizer.class);
    }

    @Test
    void createsGzipVirtualizer() {
        VirtualizerConfig config = VirtualizerConfig.gzip(50);
        JRVirtualizer virtualizer = VirtualizerFactory.create(config);
        assertThat(virtualizer).isInstanceOf(JRGzipVirtualizer.class);
    }

    @Test
    void nullConfigThrows() {
        assertThatThrownBy(() -> VirtualizerFactory.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
