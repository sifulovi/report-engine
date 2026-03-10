package com.jasperframework.registry;

import com.jasperframework.core.ReportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportRegistryTest {

    private ReportRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ReportRegistry();
    }

    @Test
    void register_shouldStoreDefinition() {
        ReportDefinition def = ReportDefinition.builder("INV-001", "reports/invoice.jrxml").build();
        registry.register(def);

        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.contains("INV-001")).isTrue();
    }

    @Test
    void lookup_shouldReturnRegisteredDefinition() {
        ReportDefinition def = ReportDefinition.builder("INV-001", "reports/invoice.jrxml").build();
        registry.register(def);

        ReportDefinition found = registry.lookup("INV-001");
        assertThat(found).isSameAs(def);
    }

    @Test
    void lookup_shouldThrowForUnknownCode() {
        assertThatThrownBy(() -> registry.lookup("UNKNOWN"))
                .isInstanceOf(ReportException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void find_shouldReturnNullForUnknownCode() {
        assertThat(registry.find("UNKNOWN")).isNull();
    }

    @Test
    void register_shouldOverwriteExisting() {
        ReportDefinition first = ReportDefinition.builder("INV-001", "reports/v1.jrxml").build();
        ReportDefinition second = ReportDefinition.builder("INV-001", "reports/v2.jrxml").build();

        registry.register(first);
        registry.register(second);

        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.lookup("INV-001").getTemplatePath()).isEqualTo("reports/v2.jrxml");
    }

    @Test
    void unregister_shouldRemoveDefinition() {
        ReportDefinition def = ReportDefinition.builder("INV-001", "reports/invoice.jrxml").build();
        registry.register(def);

        ReportDefinition removed = registry.unregister("INV-001");

        assertThat(removed).isSameAs(def);
        assertThat(registry.contains("INV-001")).isFalse();
        assertThat(registry.size()).isZero();
    }

    @Test
    void unregister_shouldReturnNullForUnknownCode() {
        assertThat(registry.unregister("UNKNOWN")).isNull();
    }

    @Test
    void getAll_shouldReturnAllDefinitions() {
        registry.register(ReportDefinition.builder("A", "a.jrxml").build());
        registry.register(ReportDefinition.builder("B", "b.jrxml").build());

        assertThat(registry.getAll()).hasSize(2);
    }

    @Test
    void getAll_shouldBeUnmodifiable() {
        registry.register(ReportDefinition.builder("A", "a.jrxml").build());

        assertThatThrownBy(() -> registry.getAll().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void clear_shouldRemoveAllDefinitions() {
        registry.register(ReportDefinition.builder("A", "a.jrxml").build());
        registry.register(ReportDefinition.builder("B", "b.jrxml").build());
        registry.clear();

        assertThat(registry.size()).isZero();
    }

    @Test
    void register_shouldRejectNull() {
        assertThatThrownBy(() -> registry.register(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
