package com.jasperframework.registry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportDefinitionTest {

    @Test
    void builder_shouldCreateDefinitionWithDefaults() {
        ReportDefinition def = ReportDefinition.builder("INV-001", "reports/invoice.jrxml")
                .build();

        assertThat(def.getCode()).isEqualTo("INV-001");
        assertThat(def.getTemplatePath()).isEqualTo("reports/invoice.jrxml");
        assertThat(def.getSupportedFormats()).containsExactlyInAnyOrder(
                ExportFormat.PDF, ExportFormat.XLSX, ExportFormat.CSV);
        assertThat(def.hasSubreports()).isFalse();
    }

    @Test
    void builder_shouldSetAllFields() {
        ReportDefinition def = ReportDefinition.builder("INV-001", "reports/invoice.jrxml")
                .displayName("Invoice Report")
                .description("Monthly invoice")
                .format(ExportFormat.PDF)
                .format(ExportFormat.XLSX)
                .subreport("ITEMS_SUBREPORT", "reports/items.jrxml")
                .build();

        assertThat(def.getDisplayName()).isEqualTo("Invoice Report");
        assertThat(def.getDescription()).isEqualTo("Monthly invoice");
        assertThat(def.getSupportedFormats()).containsExactlyInAnyOrder(
                ExportFormat.PDF, ExportFormat.XLSX);
        assertThat(def.supportsFormat(ExportFormat.CSV)).isFalse();
        assertThat(def.hasSubreports()).isTrue();
        assertThat(def.getSubreports()).hasSize(1);
        assertThat(def.getSubreports().get(0).getParameterName()).isEqualTo("ITEMS_SUBREPORT");
    }

    @Test
    void builder_shouldAcceptVarargFormats() {
        ReportDefinition def = ReportDefinition.builder("RPT", "reports/test.jrxml")
                .formats(ExportFormat.PDF, ExportFormat.CSV)
                .build();

        assertThat(def.getSupportedFormats()).containsExactlyInAnyOrder(
                ExportFormat.PDF, ExportFormat.CSV);
    }

    @Test
    void supportedFormats_shouldBeImmutable() {
        ReportDefinition def = ReportDefinition.builder("RPT", "reports/test.jrxml")
                .format(ExportFormat.PDF)
                .build();

        assertThatThrownBy(() -> def.getSupportedFormats().add(ExportFormat.CSV))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void subreports_shouldBeImmutable() {
        ReportDefinition def = ReportDefinition.builder("RPT", "reports/test.jrxml")
                .subreport("SUB", "reports/sub.jrxml")
                .build();

        assertThatThrownBy(() -> def.getSubreports().add(
                new SubreportDefinition("X", "x.jrxml")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void builder_shouldRejectBlankCode() {
        assertThatThrownBy(() -> ReportDefinition.builder("", "reports/test.jrxml"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builder_shouldRejectBlankTemplatePath() {
        assertThatThrownBy(() -> ReportDefinition.builder("RPT", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
