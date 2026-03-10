package com.jasperframework.registry;

import com.jasperframework.core.ExportFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable definition of a registered report — its unique code, template location,
 * supported export formats, and subreport relationships.
 * <p>
 * Use the {@link Builder} for construction.
 *
 * <pre>{@code
 * ReportDefinition def = ReportDefinition.builder("INV-001", "reports/invoice.jrxml")
 *         .displayName("Invoice Report")
 *         .format(ExportFormat.PDF)
 *         .format(ExportFormat.XLSX)
 *         .subreport("ORDER_ITEMS_SUBREPORT", "reports/invoice_items.jrxml")
 *         .build();
 * }</pre>
 */
public final class ReportDefinition {

    private final String code;
    private final String templatePath;
    private final String displayName;
    private final String description;
    private final Set<ExportFormat> supportedFormats;
    private final List<SubreportDefinition> subreports;

    private ReportDefinition(Builder builder) {
        this.code = builder.code;
        this.templatePath = builder.templatePath;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.supportedFormats = builder.supportedFormats.isEmpty()
                ? EnumSet.allOf(ExportFormat.class)
                : Collections.unmodifiableSet(builder.supportedFormats);
        this.subreports = Collections.unmodifiableList(new ArrayList<>(builder.subreports));
    }

    public String getCode() {
        return code;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Set<ExportFormat> getSupportedFormats() {
        return supportedFormats;
    }

    public boolean supportsFormat(ExportFormat format) {
        return supportedFormats.contains(format);
    }

    public List<SubreportDefinition> getSubreports() {
        return subreports;
    }

    public boolean hasSubreports() {
        return !subreports.isEmpty();
    }

    @Override
    public String toString() {
        return "ReportDefinition{code='" + code + "', template='" + templatePath + "'}";
    }

    public static Builder builder(String code, String templatePath) {
        return new Builder(code, templatePath);
    }

    public static final class Builder {

        private final String code;
        private final String templatePath;
        private String displayName;
        private String description;
        private final EnumSet<ExportFormat> supportedFormats = EnumSet.noneOf(ExportFormat.class);
        private final List<SubreportDefinition> subreports = new ArrayList<>();

        private Builder(String code, String templatePath) {
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("code must not be blank");
            }
            if (templatePath == null || templatePath.isBlank()) {
                throw new IllegalArgumentException("templatePath must not be blank");
            }
            this.code = code;
            this.templatePath = templatePath;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder format(ExportFormat format) {
            this.supportedFormats.add(format);
            return this;
        }

        public Builder formats(ExportFormat... formats) {
            for (ExportFormat f : formats) {
                this.supportedFormats.add(f);
            }
            return this;
        }

        public Builder subreport(String parameterName, String templatePath) {
            this.subreports.add(new SubreportDefinition(parameterName, templatePath));
            return this;
        }

        public Builder subreport(SubreportDefinition subreport) {
            this.subreports.add(subreport);
            return this;
        }

        public ReportDefinition build() {
            return new ReportDefinition(this);
        }
    }
}
