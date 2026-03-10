<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/JasperReports-7.0.3-0078D4?style=for-the-badge" alt="JasperReports 7.0.3"/>
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven 3.9+"/>
  <img src="https://img.shields.io/badge/License-TBD-blue?style=for-the-badge" alt="License"/>
</p>

<h1 align="center">Jasper Framework</h1>

<p align="center">
  <strong>Enterprise JasperReports framework — reusable across Spring Boot, Jakarta EE, Micronaut, Quarkus, and plain Java.</strong>
</p>

<p align="center">
  <a href="#-quick-start">Quick Start</a> &bull;
  <a href="#-modules">Modules</a> &bull;
  <a href="#-build-from-source">Build</a> &bull;
  <a href="#-usage">Usage</a> &bull;
  <a href="#-documentation">Docs</a> &bull;
  <a href="CONTRIBUTING.md">Contributing</a>
</p>

---

## Why Jasper Framework?

Building JasperReports into every project from scratch means duplicating boilerplate: template compilation, caching, export logic, parameter handling, and framework integration. **Jasper Framework** solves this by providing a single, well-tested library that you add as a Maven dependency.

| Problem | Solution |
|---|---|
| Rewriting report compilation logic per project | `ReportEngine` with built-in caching |
| Manual PDF/XLSX/CSV export wiring | `ExportService` with pluggable exporters |
| Hard-coded report definitions | Database-driven metadata with named SQL parameters |
| No subreport management | Automatic subreport compilation and injection |
| Framework lock-in | Zero framework deps in core; thin Spring/Jakarta adapters |
| Slow startup from runtime compilation | Maven plugin for build-time `.jrxml` -> `.jasper` |

---

## Quick Start

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
```

That's it — the engine compiles the JRXML from classpath, caches the compiled template, fills it, and returns a `JasperPrint` ready for export.

---

## Modules

```
jasper-framework (parent POM)
│
├── jasper-framework-core             Core engine (compile + fill + cache)
├── jasper-framework-registry         Report definitions & subreport resolution
├── jasper-framework-exporter         PDF / XLSX / CSV export strategies
├── jasper-framework-metadata         JDBC-based report metadata & named SQL
├── jasper-framework-composition      Multi-report merging
├── jasper-framework-async            Background job execution
├── jasper-framework-spring           Spring Boot autoconfiguration
├── jasper-framework-jakarta          Jakarta CDI integration
└── jasper-framework-maven-plugin     Build-time JRXML compiler
```

### Module Dependency Graph

```
core  (pure Java + JasperReports + SLF4J — zero framework deps)
 ├── registry       depends on: core
 ├── exporter       depends on: core
 ├── metadata       depends on: core, registry
 ├── composition    depends on: core, exporter
 ├── async          depends on: core, exporter, registry
 ├── spring         depends on: core, registry, exporter (Spring = provided)
 ├── jakarta        depends on: core, registry, exporter (CDI = provided)
 └── maven-plugin   standalone (Maven Plugin API + JasperReports)
```

### Pick What You Need

| What you need | Artifact | Transitive deps |
|---|---|---|
| Core engine only | `jasper-framework-core` | JasperReports, SLF4J |
| + Report registry | `jasper-framework-registry` | + core |
| + PDF/XLSX/CSV export | `jasper-framework-exporter` | + core |
| + Database-driven config | `jasper-framework-metadata` | + core, registry |
| + Multi-report merging | `jasper-framework-composition` | + core, exporter |
| + Background jobs | `jasper-framework-async` | + core, exporter, registry |
| Spring Boot (all-in-one) | `jasper-framework-spring` | + core, registry, exporter |
| Jakarta EE (all-in-one) | `jasper-framework-jakarta` | + core, registry, exporter |
| Build-time compilation | `jasper-framework-maven-plugin` | JasperReports |

---

## Build from Source

### Prerequisites

| Requirement | Version | Check |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.9+ (optional) | `mvn -version` |

> The project includes **Maven Wrapper** (`./mvnw`) — no Maven installation needed.

### Build Commands

```bash
# Full build + tests + install to local ~/.m2/repository
./mvnw clean install

# Skip tests (faster)
./mvnw clean install -DskipTests

# Build only JARs (no install to local repo)
./mvnw clean package

# Build + test without install
./mvnw clean verify

# Build a single module
./mvnw clean install -pl jasper-framework-core

# Build a module + all its dependencies
./mvnw clean install -pl jasper-framework-exporter -am
```

### Verify Build

After `./mvnw clean install`, all 9 module JARs are available in your local Maven repository (`~/.m2/repository/com/jasperframework/`).

```bash
# Check installed artifacts
ls ~/.m2/repository/com/jasperframework/
```

---

## Usage

### For Spring Boot Projects

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-spring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Beans auto-registered: `ReportEngine`, `ReportCompiler`, `ReportExecutor`, `ReportRegistry`, `SubreportResolver`, `ExportService` — all `@ConditionalOnMissingBean`.

```yaml
# application.yml (optional)
jasper:
  framework:
    template-prefix: reports/
    cache-enabled: true
    async-thread-pool-size: 8
```

### For Jakarta EE Projects

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

CDI producers: same beans, all `@ApplicationScoped`. Just `@Inject` and use.

### For Plain Java / Any Framework

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-exporter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
ReportEngine engine = new ReportEngine();
ExportService exportService = new ExportService();

JasperPrint print = engine.generateReport(
    ReportContext.builder("reports/invoice.jrxml")
        .parameter("invoiceId", 42)
        .build());

byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF);
Files.write(Path.of("invoice.pdf"), pdf);
```

### Maven Plugin (Build-Time Compilation)

```xml
<plugin>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals><goal>compile</goal></goals>
        </execution>
    </executions>
</plugin>
```

Compiles all `.jrxml` files to `.jasper` during `process-resources` phase.

---

## Documentation

| Document | Description |
|---|---|
| **[guideline.md](guideline.md)** | Complete developer guide — API reference, code examples, JRXML 7.x format, metadata-driven reports, Spring/Jakarta integration |
| **[CONTRIBUTING.md](CONTRIBUTING.md)** | How to contribute, coding standards, PR process, maintainer guide |
| **[requirements.md](requirements.md)** | Original requirements and architecture decisions |

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 (LTS) | Language |
| JasperReports | 7.0.3 | Report engine (Jakarta-native) |
| SLF4J | 2.0.16 | Logging facade (no binding shipped) |
| Spring Boot | 3.4.1 | Autoconfiguration (adapter only) |
| Jakarta CDI | 4.0.1 | CDI producers (adapter only) |
| JUnit 5 | 5.11.4 | Testing |
| AssertJ | 3.27.2 | Fluent test assertions |
| Mockito | 5.14.2 | Mocking |

---

## License

TBD
