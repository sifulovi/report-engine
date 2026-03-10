# Jasper Framework — Developer Guide

A reusable, enterprise-grade JasperReports library for **any** Java application: Spring Boot, Jakarta EE, Micronaut, Quarkus, or plain Java.

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [Maven Dependencies](#2-maven-dependencies)
3. [JRXML Template Format (JasperReports 7.x)](#3-jrxml-template-format-jasperreports-7x)
4. [Core Usage — Plain Java](#4-core-usage--plain-java)
5. [Report Registry & Subreports](#5-report-registry--subreports)
6. [Exporting Reports](#6-exporting-reports)
7. [Composite Reports](#7-composite-reports)
8. [Metadata-Driven Reports (Database)](#8-metadata-driven-reports-database)
9. [Async Report Generation](#9-async-report-generation)
10. [Spring Boot Integration](#10-spring-boot-integration)
11. [Jakarta EE / CDI Integration](#11-jakarta-ee--cdi-integration)
12. [Maven Plugin — Build-Time JRXML Compilation](#12-maven-plugin--build-time-jrxml-compilation)
13. [Module Dependency Graph](#13-module-dependency-graph)
14. [API Reference](#14-api-reference)

---

## 1. Quick Start

### Minimum dependency (plain Java)

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Minimum code

```java
import com.jasperframework.core.*;

ReportEngine engine = new ReportEngine();

ReportContext ctx = ReportContext.builder("reports/invoice.jrxml")
        .parameter("invoiceId", 42)
        .parameter("customerName", "Acme Corp")
        .build();

JasperPrint print = engine.generateReport(ctx);
```

That's it. The engine compiles the JRXML from the classpath, caches the compiled template, fills it with your parameters, and returns a `JasperPrint` ready for export.

---

## 2. Maven Dependencies

Pick only the modules you need. Every module declares its own transitive dependencies.

| What you need | Dependency |
|---|---|
| Core engine (compile + fill) | `jasper-framework-core` |
| Report registry + subreports | `jasper-framework-registry` |
| PDF / XLSX / CSV export | `jasper-framework-exporter` |
| Database-driven report config | `jasper-framework-metadata` |
| Merge multiple reports | `jasper-framework-composition` |
| Background job processing | `jasper-framework-async` |
| Spring Boot autoconfiguration | `jasper-framework-spring` |
| Jakarta EE / CDI producers | `jasper-framework-jakarta` |
| Build-time JRXML compilation | `jasper-framework-maven-plugin` |

**All modules share groupId `com.jasperframework`.**

### Typical Spring Boot application

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.jasperframework</groupId>
            <artifactId>jasper-framework</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.jasperframework</groupId>
        <artifactId>jasper-framework-spring</artifactId>
    </dependency>
</dependencies>
```

The Spring module transitively brings in core, registry, and exporter.

### Typical Jakarta EE application

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Minimal plain Java (core + export only)

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-exporter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 3. JRXML Template Format (JasperReports 7.x)

> **JasperReports 7.x uses a completely different JRXML format than 6.x.** Templates written for 6.x will **not** load. Use Jaspersoft Studio 7+ to create or convert templates.

### Key differences from 6.x

| 6.x Syntax | 7.x Syntax |
|---|---|
| `<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports" ...>` | `<jasperReport name="..." language="java" ...>` |
| `<title><band height="50">...</band></title>` | `<title height="50">...</title>` |
| `<textField><reportElement x="0" y="0" .../><textFieldExpression>` | `<element kind="textField" x="0" y="0" ...><expression>` |
| `<staticText><reportElement .../><text>` | `<element kind="staticText" ...><text>` |

### Minimal 7.x template

```xml
<jasperReport name="invoice" language="java"
    pageWidth="595" pageHeight="842"
    columnWidth="555" leftMargin="20" rightMargin="20"
    topMargin="20" bottomMargin="20"
    whenNoDataType="AllSectionsNoDetail">

    <parameter name="invoiceId" class="java.lang.Integer"/>
    <parameter name="customerName" class="java.lang.String"/>

    <title height="50">
        <element kind="textField" x="0" y="0" width="500" height="30">
            <expression><![CDATA["Invoice #" + $P{invoiceId}]]></expression>
        </element>
    </title>

    <detail>
        <band height="20">
            <element kind="textField" x="0" y="0" width="300" height="20">
                <expression><![CDATA[$F{itemName}]]></expression>
            </element>
            <element kind="textField" x="300" y="0" width="100" height="20">
                <expression><![CDATA[$F{amount}]]></expression>
            </element>
        </band>
    </detail>
</jasperReport>
```

### Template with subreport parameter

```xml
<parameter name="ITEMS_SUBREPORT" class="net.sf.jasperreports.engine.JasperReport"/>
```

The framework injects the compiled `JasperReport` object into this parameter automatically via `SubreportResolver` — no file paths needed.

### Where to place templates

Put `.jrxml` files on the classpath, typically:

```
src/main/resources/
  reports/
    invoice.jrxml
    invoice_items.jrxml
    order.jrxml
```

Then reference them as `"reports/invoice.jrxml"`.

---

## 4. Core Usage — Plain Java

### Basic: compile and fill

```java
ReportEngine engine = new ReportEngine();

ReportContext ctx = ReportContext.builder("reports/invoice.jrxml")
        .parameter("invoiceId", 42)
        .build();

JasperPrint print = engine.generateReport(ctx);
```

### With a JDBC connection (JasperReports runs the SQL query defined in the JRXML)

```java
ReportContext ctx = ReportContext.builder("reports/order.jrxml")
        .parameter("orderId", 100)
        .connection(dataSource.getConnection())
        .build();

JasperPrint print = engine.generateReport(ctx);
```

### With a custom JRDataSource

```java
List<Map<String, ?>> rows = List.of(
    Map.of("itemName", "Widget", "amount", 25.00),
    Map.of("itemName", "Gadget", "amount", 15.50)
);
JRDataSource ds = new JRMapCollectionDataSource(rows);

ReportContext ctx = ReportContext.builder("reports/invoice.jrxml")
        .parameter("invoiceId", 42)
        .dataSource(ds)
        .build();

JasperPrint print = engine.generateReport(ctx);
```

### Cache management

The `ReportCompiler` caches compiled templates automatically. You can manage the cache directly:

```java
ReportCompiler compiler = engine.getCompiler();

compiler.cacheSize();              // number of cached templates
compiler.evict("reports/old.jrxml"); // force recompilation next time
compiler.clearCache();              // clear everything
```

---

## 5. Report Registry & Subreports

### Register report definitions

```java
ReportRegistry registry = new ReportRegistry();

registry.register(ReportDefinition.builder("INV-001", "reports/invoice.jrxml")
        .displayName("Invoice Report")
        .description("Monthly customer invoice")
        .format(ExportFormat.PDF)
        .format(ExportFormat.XLSX)
        .subreport("ITEMS_SUBREPORT", "reports/invoice_items.jrxml")
        .build());
```

### Look up and use

```java
ReportDefinition def = registry.lookup("INV-001");  // throws if not found
ReportDefinition def = registry.find("INV-001");    // returns null if not found

boolean hasPdf = def.supportsFormat(ExportFormat.PDF);
boolean hasSubs = def.hasSubreports();
```

### Resolve subreports automatically

```java
SubreportResolver resolver = new SubreportResolver(engine.getCompiler());
Map<String, Object> subreportParams = resolver.resolveSubreports(def);
// subreportParams = {"ITEMS_SUBREPORT" -> compiled JasperReport object}

// Merge into your context parameters
ReportContext ctx = ReportContext.builder(def.getTemplatePath())
        .parameters(subreportParams)
        .parameter("invoiceId", 42)
        .connection(conn)
        .build();
```

---

## 6. Exporting Reports

### Export to PDF, XLSX, or CSV

```java
ExportService exportService = new ExportService();

// To byte array
byte[] pdf  = exportService.exportToBytes(print, ExportFormat.PDF);
byte[] xlsx = exportService.exportToBytes(print, ExportFormat.XLSX);
byte[] csv  = exportService.exportToBytes(print, ExportFormat.CSV);

// To output stream
try (OutputStream out = new FileOutputStream("invoice.pdf")) {
    exportService.export(print, ExportFormat.PDF, out);
}
```

### Full pipeline: compile → fill → export

```java
ReportEngine engine = new ReportEngine();
ExportService exportService = new ExportService();

JasperPrint print = engine.generateReport(
        ReportContext.builder("reports/invoice.jrxml")
                .parameter("invoiceId", 42)
                .build());

byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF);

// Write to file, send as HTTP response, store in S3, etc.
Files.write(Path.of("invoice.pdf"), pdf);
```

### Register a custom exporter

```java
exportService.register(new ReportExporter() {
    @Override
    public void export(JasperPrint print, OutputStream output) {
        // your custom logic (e.g., HTML, DOCX)
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.PDF; // replaces the default PDF exporter
    }
});
```

---

## 7. Composite Reports

Merge multiple filled reports into a single document (e.g., a financial package with Balance Sheet + Income Statement + Cash Flow).

```java
ReportEngine engine = new ReportEngine();

JasperPrint balanceSheet = engine.generateReport(
        ReportContext.builder("reports/balance_sheet.jrxml")
                .connection(conn).build());
JasperPrint incomeStmt = engine.generateReport(
        ReportContext.builder("reports/income_statement.jrxml")
                .connection(conn).build());
JasperPrint cashFlow = engine.generateReport(
        ReportContext.builder("reports/cash_flow.jrxml")
                .connection(conn).build());

// Option A: Compose then export separately
ReportComposer composer = new ReportComposer();
JasperPrint merged = composer.compose("Financial Report",
        balanceSheet, incomeStmt, cashFlow);

byte[] pdf = new ExportService().exportToBytes(merged, ExportFormat.PDF);

// Option B: One-step merge + export
ReportMergeService mergeService = new ReportMergeService(
        new ReportComposer(), new ExportService());

byte[] pdf = mergeService.mergeAndExport("Financial Report",
        ExportFormat.PDF, balanceSheet, incomeStmt, cashFlow);
```

---

## 8. Metadata-Driven Reports (Database)

Load report definitions, parameters, and queries from database tables instead of hard-coding them.

### 1. Create the schema

Run the SQL from `jasper-framework-metadata/src/main/resources/schema/report-metadata.sql`:

```sql
CREATE TABLE reports (
    code          VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    template_path VARCHAR(500) NOT NULL,
    query         TEXT,
    active        BOOLEAN DEFAULT TRUE
);

CREATE TABLE report_parameters (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code   VARCHAR(50) NOT NULL REFERENCES reports(code),
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(100) NOT NULL DEFAULT 'java.lang.String',
    required      BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(500)
);

CREATE TABLE report_subreports (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code     VARCHAR(50) NOT NULL REFERENCES reports(code),
    parameter_name  VARCHAR(100) NOT NULL,
    template_path   VARCHAR(500) NOT NULL
);
```

### 2. Insert report metadata

```sql
INSERT INTO reports VALUES (
    'INV-001', 'Invoice', 'Customer invoice',
    'reports/invoice.jrxml',
    'SELECT * FROM invoice_items WHERE invoice_id = :invoiceId',
    true
);

INSERT INTO report_parameters VALUES
    (1, 'INV-001', 'invoiceId', 'java.lang.Long', true, null);
```

### 3. Generate from metadata

```java
ReportEngine engine = new ReportEngine();
ReportMetadataService metadataService = new ReportMetadataService(
        dataSource, engine, engine.getCompiler());

// This will: load metadata → validate params → execute SQL query →
// convert results to JRDataSource → resolve subreports → fill report
JasperPrint print = metadataService.generateFromMetadata("INV-001",
        Map.of("invoiceId", 42L));
```

### Named parameter support in queries

The `QueryExecutor` supports `:paramName` syntax:

```sql
SELECT * FROM orders WHERE customer_id = :customerId AND status = :status
```

Parameters are bound by name from the map you pass in.

### Loading metadata directly

```java
ReportMetadataLoader loader = new ReportMetadataLoader(dataSource);

ReportMetadata meta = loader.load("INV-001");
// meta.getCode(), meta.getTemplatePath(), meta.getQuery()
// meta.getParameters(), meta.getSubreports()

List<ReportMetadata> active = loader.loadAllActive();
```

---

## 9. Async Report Generation

Run reports in the background using a `java.util.concurrent` thread pool. No framework dependencies — works everywhere.

```java
ReportEngine engine = new ReportEngine();
ExportService exportService = new ExportService();
ReportWorker worker = new ReportWorker(engine, exportService);

// 4-thread pool (daemon threads)
try (ReportJobService jobService = new ReportJobService(worker, 4)) {

    // Submit a job (returns immediately)
    ReportJob job = jobService.submit("INV-001", "reports/invoice.jrxml",
            ExportFormat.PDF, Map.of("invoiceId", 42));

    System.out.println("Job ID: " + job.getId());
    System.out.println("Status: " + job.getStatus()); // PENDING or RUNNING

    // ... do other work ...

    // Poll for completion
    ReportJob result = jobService.getJob(job.getId());
    if (result.getStatus() == JobStatus.COMPLETED) {
        byte[] pdf = result.getResult();
        Files.write(Path.of("invoice.pdf"), pdf);
    } else if (result.getStatus() == JobStatus.FAILED) {
        System.err.println("Error: " + result.getErrorMessage());
    }

    // List all jobs
    Collection<ReportJob> allJobs = jobService.getAllJobs();
}
// close() shuts down the pool gracefully
```

### Job lifecycle

```
PENDING → RUNNING → COMPLETED (result bytes available)
                  → FAILED    (errorMessage available)
```

---

## 10. Spring Boot Integration

### Setup

Add the dependency — autoconfiguration handles the rest:

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-spring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Auto-registered beans

The following beans are registered automatically (all `@ConditionalOnMissingBean` — you can override any of them):

- `ReportCompiler`
- `ReportExecutor`
- `ReportEngine`
- `ReportRegistry`
- `SubreportResolver`
- `ExportService`
- `JasperFrameworkProperties`

### Configuration properties

```yaml
jasper:
  framework:
    template-prefix: reports/
    cache-enabled: true
    async-thread-pool-size: 8
```

### Example: REST controller

```java
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired private ReportEngine engine;
    @Autowired private ExportService exportService;

    @PostMapping("/{code}")
    public ResponseEntity<byte[]> generate(
            @PathVariable String code,
            @RequestParam ExportFormat format,
            @RequestBody Map<String, Object> params) {

        ReportContext ctx = ReportContext.builder("reports/" + code + ".jrxml")
                .parameters(params)
                .build();

        JasperPrint print = engine.generateReport(ctx);
        byte[] data = exportService.exportToBytes(print, format);

        String contentType = switch (format) {
            case PDF  -> "application/pdf";
            case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV  -> "text/csv";
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "attachment; filename=report." + format.name().toLowerCase())
                .body(data);
    }
}
```

### Override a default bean

```java
@Configuration
public class CustomReportConfig {

    @Bean
    public ExportService exportService() {
        ExportService service = new ExportService();
        service.register(new MyCustomHtmlExporter());
        return service;
    }
}
```

---

## 11. Jakarta EE / CDI Integration

### Setup

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Auto-produced beans (via CDI `@Produces`)

- `ReportCompiler`
- `ReportExecutor`
- `ReportEngine`
- `ReportRegistry`
- `SubreportResolver`
- `ExportService`

All are `@ApplicationScoped`.

### Example: JSF backing bean

```java
@Named
@ViewScoped
public class InvoiceBean implements Serializable {

    @Inject private ReportEngine engine;
    @Inject private ExportService exportService;

    public void generatePdf() throws IOException {
        ReportContext ctx = ReportContext.builder("reports/invoice.jrxml")
                .parameter("invoiceId", selectedInvoiceId)
                .build();

        JasperPrint print = engine.generateReport(ctx);
        byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF);

        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
        response.getOutputStream().write(pdf);
        fc.responseComplete();
    }
}
```

### Using with PrimeFaces `<p:fileDownload>`

```java
public StreamedContent getInvoiceDownload() {
    JasperPrint print = engine.generateReport(
            ReportContext.builder("reports/invoice.jrxml")
                    .parameter("invoiceId", selectedId).build());
    byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF);

    return DefaultStreamedContent.builder()
            .name("invoice.pdf")
            .contentType("application/pdf")
            .stream(() -> new ByteArrayInputStream(pdf))
            .build();
}
```

---

## 12. Maven Plugin — Build-Time JRXML Compilation

Pre-compile `.jrxml` to `.jasper` during the build to catch template errors early and speed up runtime.

### Configuration

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.jasperframework</groupId>
            <artifactId>jasper-framework-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <!-- defaults shown; override as needed -->
                <sourceDirectory>${project.basedir}/src/main/resources/reports</sourceDirectory>
                <outputDirectory>${project.build.outputDirectory}/reports</outputDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### What it does

- Scans `sourceDirectory` for `*.jrxml` files
- Compiles each to a `*.jasper` binary in `outputDirectory`
- Runs during the `process-resources` phase by default
- Fails the build if any template has errors

---

## 13. Module Dependency Graph

```
jasper-framework-core  (pure Java + JasperReports + SLF4J)
  │
  ├── jasper-framework-registry    (+ core)
  ├── jasper-framework-exporter    (+ core + jasperreports-pdf + jasperreports-excel-poi)
  │
  ├── jasper-framework-metadata    (+ core + registry)
  ├── jasper-framework-composition (+ core + exporter)
  ├── jasper-framework-async       (+ core + exporter + registry)
  │
  ├── jasper-framework-spring      (+ core + registry + exporter; Spring Boot = provided)
  ├── jasper-framework-jakarta     (+ core + registry + exporter; Jakarta CDI = provided)
  │
  └── jasper-framework-maven-plugin (standalone; Maven Plugin API + JasperReports)
```

**Design principles:**
- `core` has **zero** framework dependencies — only JasperReports and SLF4J
- Spring and Jakarta modules use `provided` scope — your app supplies the runtime
- SLF4J is the logging facade — **no logging implementation is shipped**; your app chooses (Logback, Log4j2, etc.)

---

## 14. API Reference

### `com.jasperframework.core`

| Class | Key Methods |
|---|---|
| `ReportEngine` | `generateReport(ReportContext)`, `getCompiler()`, `getExecutor()` |
| `ReportCompiler` | `compile(String)`, `getCached(String)`, `evict(String)`, `clearCache()`, `cacheSize()` |
| `ReportExecutor` | `execute(ReportTemplate, ReportContext)` |
| `ReportContext.Builder` | `builder(String templatePath)`, `parameter(K,V)`, `parameters(Map)`, `dataSource(JRDataSource)`, `connection(Connection)`, `build()` |
| `ReportTemplate` | `getTemplatePath()`, `getCompiledReport()` |
| `ExportFormat` | `PDF`, `XLSX`, `CSV` |

### `com.jasperframework.registry`

| Class | Key Methods |
|---|---|
| `ReportRegistry` | `register(ReportDefinition)`, `lookup(String)`, `find(String)`, `contains(String)`, `unregister(String)`, `getAll()`, `size()`, `clear()` |
| `ReportDefinition.Builder` | `builder(code, templatePath)`, `displayName(String)`, `description(String)`, `format(ExportFormat)`, `formats(ExportFormat...)`, `subreport(param, path)`, `build()` |
| `SubreportResolver` | `resolveSubreports(ReportDefinition)` → `Map<String, Object>` |
| `SubreportDefinition` | `getParameterName()`, `getTemplatePath()` |

### `com.jasperframework.exporter`

| Class | Key Methods |
|---|---|
| `ExportService` | `export(JasperPrint, ExportFormat, OutputStream)`, `exportToBytes(JasperPrint, ExportFormat)`, `register(ReportExporter)`, `supportsFormat(ExportFormat)` |
| `ReportExporter` (interface) | `export(JasperPrint, OutputStream)`, `getFormat()` |
| `PdfExporter` | implements `ReportExporter` |
| `XlsxExporter` | implements `ReportExporter` |
| `CsvExporter` | implements `ReportExporter` |

### `com.jasperframework.metadata`

| Class | Key Methods |
|---|---|
| `ReportMetadataService` | `generateFromMetadata(String reportCode, Map<String, Object> params)` |
| `ReportMetadataLoader` | `load(String reportCode)`, `loadAllActive()` |
| `QueryExecutor` | `execute(String sql, Map<String, Object> params)` → `JRDataSource` |
| `ReportMetadata` | `getCode()`, `getTemplatePath()`, `getQuery()`, `getParameters()`, `getSubreports()`, `isActive()` |

### `com.jasperframework.composition`

| Class | Key Methods |
|---|---|
| `ReportComposer` | `compose(String name, JasperPrint... reports)`, `compose(String name, List<JasperPrint>)` |
| `ReportMergeService` | `mergeAndExport(name, format, reports...)` → `byte[]` |

### `com.jasperframework.async`

| Class | Key Methods |
|---|---|
| `ReportJobService` | `submit(code, templatePath, format, params)` → `ReportJob`, `getJob(String id)`, `getAllJobs()`, `close()` |
| `ReportJob` | `getId()`, `getStatus()`, `getResult()`, `getErrorMessage()`, `getCreatedAt()`, `getCompletedAt()` |
| `JobStatus` | `PENDING`, `RUNNING`, `COMPLETED`, `FAILED` |

### Exception hierarchy

```
RuntimeException
  └── ReportException
        ├── ReportCompilationException   (JRXML not found or invalid)
        ├── ReportExecutionException     (report filling failed)
        └── ReportExportException        (export failed)
```

All exceptions are unchecked. Catch `ReportException` to handle any framework error.
