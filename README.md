<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/JasperReports-7.0.3-0078D4?style=for-the-badge" alt="JasperReports 7.0.3"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.4.1"/>
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven 3.9+"/>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License"/>
</p>

<h1 align="center">📊 Jasper Framework</h1>

<p align="center">
  <strong>Enterprise-grade JasperReports engine — one library for Spring Boot, Jakarta EE, Micronaut, Quarkus, and plain Java.</strong>
</p>

<p align="center">
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-features">Features</a> •
  <a href="#-modules">Modules</a> •
  <a href="#-installation">Installation</a> •
  <a href="#-usage-examples">Usage</a> •
  <a href="#-documentation">Docs</a> •
  <a href="CONTRIBUTING.md">Contributing</a>
</p>

---

## ✨ Features

<table>
<tr>
<td width="50%">

🔧 **Zero Framework Lock-in**
Core engine has zero framework dependencies — works in any Java app

⚡ **Async Report Generation**
Background job processing with thread pool and job lifecycle tracking

📦 **Build-Time Compilation**
Maven plugin compiles JRXML → .jasper during build to catch errors early

🔒 **Template Caching**
Thread-safe compiled template cache with eviction support

📄 **Pagination Support**
Export specific page ranges from any report

</td>
<td width="50%">

📊 **Multi-Format Export**
PDF, XLSX, CSV out of the box — pluggable for custom formats

🗄️ **Metadata-Driven Reports**
Database-driven report definitions with named SQL parameters

🔌 **Auto-Integration**
Spring Boot autoconfiguration & Jakarta CDI producers — just add dependency

💾 **Pluggable Storage**
File system implementation included, interface for S3/MinIO/custom

🧩 **Composite Report Merging**
Combine multiple reports into a single document

</td>
</tr>
</table>

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Your Application                           │
│  (Spring Boot / Jakarta EE / Micronaut / Quarkus / Plain Java)  │
└──────────┬──────────────────────────────────┬───────────────────┘
           │                                  │
    ┌──────▼──────┐                   ┌───────▼───────┐
    │   spring    │                   │    jakarta    │
    │  (adapter)  │                   │   (adapter)   │
    └──────┬──────┘                   └───────┬───────┘
           │          ┌───────────┐           │
           └──────────►   core    ◄───────────┘
                      │  engine   │
                      └─────┬─────┘
           ┌────────┬───────┼───────┬──────────┐
           ▼        ▼       ▼       ▼          ▼
      ┌────────┐┌───────┐┌─────┐┌───────┐┌────────┐
      │registry││export ││meta ││compose││ async  │
      │        ││ PDF   ││data ││ merge ││  jobs  │
      │        ││ XLSX  ││ SQL ││       ││        │
      │        ││ CSV   ││     ││       ││        │
      └────────┘└───────┘└─────┘└───────┘└────────┘
                                              │
                                        ┌─────▼─────┐
                                        │  storage   │
                                        │ filesystem │
                                        └───────────┘
```

---

## 🚀 Quick Start

### 1. Build & install

```bash
git clone https://github.com/sifulovi/report-engine.git
cd report-engine
./mvnw clean install
```

### 2. Add to your project

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 3. Generate a report

```java
ReportEngine engine = new ReportEngine();

ReportContext ctx = ReportContext.builder("reports/invoice.jrxml")
        .parameter("invoiceId", 42)
        .parameter("customerName", "Acme Corp")
        .build();

JasperPrint print = engine.generateReport(ctx);
// → compiled from classpath, cached, filled with parameters, ready for export
```

---

## 📦 Modules

| | Module | Description | Key Classes |
|---|---|---|---|
| 🔧 | **jasper-framework-core** | Compile, cache, and fill reports | `ReportEngine`, `ReportCompiler`, `ReportExecutor` |
| 📋 | **jasper-framework-registry** | Report definitions & subreport resolution | `ReportRegistry`, `ReportDefinition`, `SubreportResolver` |
| 📤 | **jasper-framework-exporter** | PDF / XLSX / CSV export with pagination | `ExportService`, `PageRange`, `PdfExporter` |
| 🗄️ | **jasper-framework-metadata** | Database-driven report configuration | `ReportMetadataService`, `QueryExecutor` |
| 🧩 | **jasper-framework-composition** | Merge multiple reports into one | `ReportComposer`, `ReportMergeService` |
| ⚡ | **jasper-framework-async** | Background job processing | `ReportJobService`, `ReportWorker`, `ReportJob` |
| 💾 | **jasper-framework-storage** | Report file storage abstraction | `ReportStorageService`, `FileSystemStorageService` |
| 🍃 | **jasper-framework-spring** | Spring Boot autoconfiguration + REST API | `JasperFrameworkAutoConfiguration`, `ReportRestController` |
| ☕ | **jasper-framework-jakarta** | Jakarta EE CDI integration | `JasperFrameworkProducer` |
| 🔨 | **jasper-framework-maven-plugin** | Build-time JRXML → .jasper compilation | `CompileMojo` |
| 📝 | **jasper-framework-example** | Spring Boot demo application | Invoice, Product, Order reports |

---

## 📥 Installation

### 🍃 Spring Boot

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-spring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
> Autoconfigures `ReportEngine`, `ExportService`, `ReportRegistry`, and REST endpoints.

### ☕ Jakarta EE

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
> CDI producers for all core beans — just `@Inject` and use.

### 🔧 Plain Java (Core + Export)

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-exporter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
> Brings in core engine + all three exporters. No framework deps.

---

## 💻 Usage Examples

<details>
<summary><b>📤 Export to PDF / XLSX / CSV</b></summary>

```java
ReportEngine engine = new ReportEngine();
ExportService exportService = new ExportService();

JasperPrint print = engine.generateReport(
    ReportContext.builder("reports/invoice.jrxml")
        .parameter("invoiceId", 42)
        .build());

byte[] pdf  = exportService.exportToBytes(print, ExportFormat.PDF);
byte[] xlsx = exportService.exportToBytes(print, ExportFormat.XLSX);
byte[] csv  = exportService.exportToBytes(print, ExportFormat.CSV);

Files.write(Path.of("invoice.pdf"), pdf);
```

</details>

<details>
<summary><b>🗄️ Metadata-Driven Report Generation</b></summary>

```sql
-- 1. Set up metadata tables and insert report config
INSERT INTO reports VALUES ('INV-001', 'Invoice', 'Customer invoice',
    'reports/invoice.jrxml',
    'SELECT * FROM invoice_items WHERE invoice_id = :invoiceId', true);

INSERT INTO report_parameters VALUES
    (1, 'INV-001', 'invoiceId', 'java.lang.Long', true, null);
```

```java
// 2. Generate from metadata — loads config, validates params, runs SQL, fills report
ReportMetadataService service = new ReportMetadataService(dataSource, engine, compiler);
JasperPrint print = service.generateFromMetadata("INV-001", Map.of("invoiceId", 42L));
```

</details>

<details>
<summary><b>⚡ Async Report Generation</b></summary>

```java
ReportWorker worker = new ReportWorker(engine, exportService);

try (ReportJobService jobService = new ReportJobService(worker, 4)) {
    // Submit (returns immediately)
    ReportJob job = jobService.submit("INV-001", "reports/invoice.jrxml",
            ExportFormat.PDF, Map.of("invoiceId", 42));

    // Poll later
    ReportJob result = jobService.getJob(job.getId());
    if (result.getStatus() == JobStatus.COMPLETED) {
        byte[] pdf = result.getResult();
    }
}
```

</details>

<details>
<summary><b>🧩 Composite Reports</b></summary>

```java
JasperPrint balanceSheet = engine.generateReport(ctx1);
JasperPrint incomeStmt   = engine.generateReport(ctx2);
JasperPrint cashFlow     = engine.generateReport(ctx3);

ReportMergeService mergeService = new ReportMergeService(
    new ReportComposer(), new ExportService());

byte[] pdf = mergeService.mergeAndExport("Financial Report",
    ExportFormat.PDF, balanceSheet, incomeStmt, cashFlow);
```

</details>

<details>
<summary><b>🍃 Spring Boot REST API</b></summary>

The framework auto-registers REST endpoints when Spring Web is on the classpath:

```bash
# Generate report by code (POST)
curl -X POST "http://localhost:8080/reports/invoice?format=PDF" -o invoice.pdf

# Check async job status (GET)
curl http://localhost:8080/reports/job/abc-123
```

```yaml
# application.yml
jasper:
  framework:
    template-prefix: reports/
    cache-enabled: true
    async-thread-pool-size: 8
```

</details>

<details>
<summary><b>📄 Paginated Export</b></summary>

```java
// Export only pages 0-4 (first 5 pages)
byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF, PageRange.of(0, 4));

// Export a single page
byte[] page = exportService.exportToBytes(print, ExportFormat.PDF, PageRange.single(2));
```

</details>

<details>
<summary><b>💾 Report Storage</b></summary>

```java
ReportStorageService storage = new FileSystemStorageService(Paths.get("/reports"));

storage.store("invoices/2024/INV-001.pdf", pdfBytes);

try (InputStream in = storage.retrieve("invoices/2024/INV-001.pdf")) {
    // stream to client
}

storage.exists("invoices/2024/INV-001.pdf"); // true
storage.delete("invoices/2024/INV-001.pdf");
```

</details>

---

## ⚙️ Configuration

### Spring Boot Properties

| Property | Default | Description |
|---|---|---|
| `jasper.framework.template-prefix` | `reports/` | Classpath prefix for JRXML templates |
| `jasper.framework.cache-enabled` | `true` | Enable compiled template caching |
| `jasper.framework.async-thread-pool-size` | CPU cores | Thread pool size for async jobs |

---

## 🛠️ Build from Source

### Prerequisites

| Requirement | Version | Check |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.9+ (optional) | `mvn -version` |

> 💡 The project includes **Maven Wrapper** (`./mvnw`) — no Maven installation needed.

### Build Commands

```bash
./mvnw clean install          # Full build + tests + install to ~/.m2
./mvnw clean install -DskipTests  # Skip tests (faster)
./mvnw clean verify            # Build + test without install
./mvnw clean install -pl jasper-framework-core -am  # Single module + deps
```

---

## 🔧 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| ☕ Java | 17 (LTS) | Language target |
| 📊 JasperReports | 7.0.3 | Report engine (Jakarta-native) |
| 📝 SLF4J | 2.0.16 | Logging facade (no binding shipped) |
| 🍃 Spring Boot | 3.4.1 | Autoconfiguration adapter |
| ☕ Jakarta CDI | 4.0.1 | CDI producer adapter |
| ✅ JUnit 5 | 5.11.4 | Testing |
| 🔍 AssertJ | 3.27.2 | Fluent test assertions |
| 🎭 Mockito | 5.14.2 | Mocking |

---

## 📂 Project Structure

```
jasper-framework/
├── jasper-framework-core/           🔧 Core engine (compile + fill + cache)
├── jasper-framework-registry/       📋 Report definitions & subreport resolution
├── jasper-framework-exporter/       📤 PDF / XLSX / CSV exporters + pagination
├── jasper-framework-metadata/       🗄️ JDBC metadata loader & SQL query executor
├── jasper-framework-composition/    🧩 Multi-report merging
├── jasper-framework-async/          ⚡ Background job execution
├── jasper-framework-storage/        💾 Report file storage abstraction
├── jasper-framework-spring/         🍃 Spring Boot autoconfiguration + REST
├── jasper-framework-jakarta/        ☕ Jakarta CDI integration
├── jasper-framework-maven-plugin/   🔨 Build-time JRXML compiler
├── jasper-framework-example/        📝 Spring Boot demo application
├── pom.xml                          Parent POM (version management)
├── guideline.md                     📖 Complete developer guide
├── CONTRIBUTING.md                  🤝 Contribution guide
└── requirements.md                  📋 Original requirements
```

---

## 📖 Documentation

| Document | Description |
|---|---|
| 📖 **[Developer Guide](guideline.md)** | Complete guide — API reference, code examples, JRXML 7.x format, metadata-driven reports, troubleshooting, best practices |
| 🤝 **[Contributing](CONTRIBUTING.md)** | How to contribute — coding standards, branch naming, PR process, maintainer guide |
| 📋 **[Requirements](requirements.md)** | Original requirements and architecture decisions |

---

## 🤝 Contributing

We welcome contributions! Please read our [Contributing Guide](CONTRIBUTING.md) for details on:

- 🔀 Branch naming conventions
- 📝 Commit message format
- ✅ PR checklist
- 🎨 Coding standards
- 🧪 Testing requirements

---

## 📄 License

MIT
