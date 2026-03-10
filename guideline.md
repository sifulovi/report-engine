# 📖 Jasper Framework — Complete Developer Guide

A comprehensive guide for building production reports with the Jasper Framework. Written for developers of all experience levels.

---

## 📑 Table of Contents

1. [Introduction & Architecture](#1--introduction--architecture)
2. [Prerequisites & Setup](#2--prerequisites--setup)
3. [Quick Start (5 Minutes)](#3--quick-start-5-minutes)
4. [Maven Dependencies](#4--maven-dependencies)
5. [JRXML Template Format (JR 7.x)](#5--jrxml-template-format-jr-7x)
6. [Core Engine](#6--core-engine)
7. [Report Registry & Subreports](#7--report-registry--subreports)
8. [Exporting Reports](#8--exporting-reports)
9. [Composite Reports](#9--composite-reports)
10. [Metadata-Driven Reports](#10--metadata-driven-reports)
11. [Async Report Generation](#11--async-report-generation)
12. [Report Storage](#12--report-storage)
13. [Performance Features](#13--performance-features)
14. [Spring Boot Integration](#14--spring-boot-integration)
15. [Jakarta EE / CDI Integration](#15--jakarta-ee--cdi-integration)
16. [Maven Plugin](#16--maven-plugin)
17. [Module Dependency Graph](#17--module-dependency-graph)
18. [API Reference](#18--api-reference)
19. [Exception Hierarchy](#19--exception-hierarchy)
20. [Troubleshooting & FAQ](#20--troubleshooting--faq)
21. [Best Practices](#21--best-practices)

---

## 1. 🏗️ Introduction & Architecture

### What is Jasper Framework?

Jasper Framework is a **reusable Java library** that wraps JasperReports into a clean, modular API. Instead of rewriting report compilation, caching, export, and framework integration in every project, you add a single Maven dependency and get a production-ready reporting engine.

### Why use it?

| Problem | What you'd normally do | What Jasper Framework does |
|---|---|---|
| Template compilation | Write boilerplate `JasperCompileManager` calls | `ReportEngine.generateReport()` — one call |
| Caching compiled templates | Build your own `ConcurrentHashMap` wrapper | Built-in thread-safe cache in `ReportCompiler` |
| Multi-format export | Wire up each exporter manually | `ExportService` — pluggable PDF/XLSX/CSV |
| Framework integration | Write Spring beans or CDI producers manually | Auto-registered via `jasper-framework-spring` or `jasper-framework-jakarta` |
| Report metadata | Hard-code report definitions in Java | Database-driven with `ReportMetadataService` |

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        YOUR APPLICATION                             │
│   (Spring Boot / Jakarta EE / Micronaut / Quarkus / Plain Java)    │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
  ┌──────▼──────┐       ┌─────▼─────┐        ┌──────▼──────┐
  │   spring    │       │   core    │        │   jakarta   │
  │  (adapter)  │       │  engine   │        │  (adapter)  │
  └─────────────┘       └─────┬─────┘        └─────────────┘
                              │
        ┌──────────┬──────────┼──────────┬──────────┐
        │          │          │          │          │
   ┌────▼───┐ ┌───▼────┐ ┌───▼───┐ ┌───▼────┐ ┌───▼───┐
   │registry│ │exporter│ │metadata│ │compose │ │ async │
   │        │ │PDF/XLSX│ │SQL/DB  │ │ merge  │ │ jobs  │
   │        │ │CSV     │ │        │ │        │ │       │
   └────────┘ └────────┘ └───────┘ └────────┘ └───┬───┘
                                                   │
                                             ┌─────▼─────┐
                                             │  storage   │
                                             │ filesystem │
                                             └───────────┘
```

### How a report flows through the system

```
1. JRXML Template        2. Compile & Cache       3. Fill with Data
   (classpath)              (ReportCompiler)         (ReportExecutor)
   ┌──────────┐          ┌──────────────────┐     ┌──────────────────┐
   │ .jrxml   │  ──────► │  JasperReport    │ ──► │   JasperPrint    │
   │ template │          │  (compiled,      │     │   (filled pages) │
   └──────────┘          │   cached)        │     └────────┬─────────┘
                         └──────────────────┘              │
                                                           ▼
                                              4. Export (ExportService)
                                              ┌──────────────────────┐
                                              │  byte[] PDF/XLSX/CSV │
                                              └──────────────────────┘
```

---

## 2. 🔧 Prerequisites & Setup

### System Requirements

| Requirement | Minimum Version | How to check |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.9+ (optional) | `mvn -version` |
| Git | Any recent version | `git --version` |

> 💡 **Tip:** The project includes Maven Wrapper (`./mvnw`), so you don't need Maven installed globally.

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/sifulovi/report-engine.git
cd report-engine

# Build everything (first time takes ~2 minutes to download dependencies)
./mvnw clean install

# Verify all tests pass
./mvnw clean verify
```

### IDE Setup

**IntelliJ IDEA (recommended):**
1. File → Open → select the root `pom.xml` → "Open as Project"
2. Wait for Maven import to finish (watch the progress bar)
3. Verify: Project Structure → Project SDK → Java 17+
4. Verify: all 11 modules appear in the Maven tool window

**Eclipse:**
1. File → Import → Maven → Existing Maven Projects
2. Browse to the root directory → Finish
3. Wait for workspace build

**VS Code:**
1. Install "Extension Pack for Java" + "Maven for Java"
2. Open the root folder
3. Java projects will auto-detect from `pom.xml`

> ⚠️ **Warning:** If you see red squiggles after import, try Maven → Reimport or `./mvnw clean install` from terminal first.

---

## 3. 🚀 Quick Start (5 Minutes)

### Step 1: Add the dependency

For the simplest setup (core engine only):

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

If you also want PDF/XLSX/CSV export:

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-exporter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Step 2: Create a JRXML template

Save this as `src/main/resources/reports/hello.jrxml`:

```xml
<jasperReport name="hello" language="java"
    pageWidth="595" pageHeight="842"
    columnWidth="555" leftMargin="20" rightMargin="20"
    topMargin="20" bottomMargin="20"
    whenNoDataType="AllSectionsNoDetail">

    <parameter name="greeting" class="java.lang.String"/>

    <title height="50">
        <element kind="textField" x="0" y="0" width="555" height="30"
                 fontSize="20.0" bold="true" hTextAlign="Center">
            <expression><![CDATA[$P{greeting}]]></expression>
        </element>
    </title>
</jasperReport>
```

### Step 3: Generate and export

```java
import com.jasperframework.core.*;
import com.jasperframework.exporter.*;

public class QuickStartDemo {
    public static void main(String[] args) throws Exception {
        // 1. Create the engine (thread-safe, reuse across your app)
        ReportEngine engine = new ReportEngine();
        ExportService exportService = new ExportService();

        // 2. Build context with template path and parameters
        ReportContext ctx = ReportContext.builder("reports/hello.jrxml")
                .parameter("greeting", "Hello from Jasper Framework!")
                .build();

        // 3. Generate (compile + fill)
        JasperPrint print = engine.generateReport(ctx);

        // 4. Export to PDF
        byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF);

        // 5. Save to file
        java.nio.file.Files.write(java.nio.file.Path.of("hello.pdf"), pdf);
        System.out.println("Generated hello.pdf (" + pdf.length + " bytes)");
    }
}
```

> 💡 **What just happened?**
> - `ReportEngine` compiled `hello.jrxml` from the classpath and cached the compiled template
> - The `greeting` parameter was injected into the template
> - `ExportService` converted the filled report to PDF bytes
> - Next time you call `generateReport()` with the same template, it uses the cached version (no recompilation)

---

## 4. 📦 Maven Dependencies

### Module Selector

Choose the modules you need. Every module declares its own transitive dependencies — you won't get more than you asked for.

| What you need | Artifact | What it brings in |
|---|---|---|
| Core engine only | `jasper-framework-core` | JasperReports + SLF4J |
| + Report registry & subreports | `jasper-framework-registry` | + core |
| + PDF / XLSX / CSV export | `jasper-framework-exporter` | + core + JR PDF/Excel libs |
| + Database-driven config | `jasper-framework-metadata` | + core + registry |
| + Multi-report merging | `jasper-framework-composition` | + core + exporter |
| + Background jobs | `jasper-framework-async` | + core + exporter + registry |
| + File storage | `jasper-framework-storage` | SLF4J (no JR dependency) |
| Spring Boot (all-in-one) | `jasper-framework-spring` | + core + registry + exporter |
| Jakarta EE (all-in-one) | `jasper-framework-jakarta` | + core + registry + exporter |
| Build-time compilation | `jasper-framework-maven-plugin` | JasperReports (standalone) |

**All modules share groupId `com.jasperframework` and version `1.0.0-SNAPSHOT`.**

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

### Typical Jakarta EE application

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 5. 📝 JRXML Template Format (JR 7.x)

> ⚠️ **Critical:** JasperReports 7.x uses a **completely different JRXML format** than 6.x. Templates written for 6.x **will not work**. This is the most common source of errors for new developers.

### 6.x vs 7.x Comparison

| Feature | 6.x Syntax ❌ | 7.x Syntax ✅ |
|---|---|---|
| Root element | `<jasperReport xmlns="http://jasperreports.sourceforge.net/..."  >` | `<jasperReport name="..." language="java" ...>` |
| Title band | `<title><band height="50">...</band></title>` | `<title height="50">...</title>` |
| Text field | `<textField><reportElement x="0" y="0" .../><textFieldExpression>` | `<element kind="textField" x="0" y="0" ...><expression>` |
| Static text | `<staticText><reportElement .../><text>` | `<element kind="staticText" ...><text>` |
| Detail band | `<detail><band height="20">...</band></detail>` | `<detail><band height="20">...</band></detail>` (same) |

### Template Structure

Every JRXML template follows this structure (elements must appear in this order):

```xml
<jasperReport name="..." language="java" pageWidth="595" pageHeight="842"
    columnWidth="555" leftMargin="20" rightMargin="20"
    topMargin="20" bottomMargin="20"
    whenNoDataType="AllSectionsNoDetail">

    <!-- 1. Parameters (input values from Java code) -->
    <parameter name="paramName" class="java.lang.String"/>

    <!-- 2. Fields (columns from data source) -->
    <field name="fieldName" class="java.lang.String"/>

    <!-- 3. Variables (calculated values) -->
    <variable name="total" calculation="Sum" class="java.lang.Double">
        <expression><![CDATA[$F{amount}]]></expression>
    </variable>

    <!-- 4. Title band (first page header) -->
    <title height="50">
        <element kind="textField" x="0" y="0" width="555" height="30">
            <expression><![CDATA[$P{paramName}]]></expression>
        </element>
    </title>

    <!-- 5. Column header (repeats on each page) -->
    <columnHeader height="25">
        <element kind="staticText" x="0" y="0" width="200" height="20" bold="true">
            <text><![CDATA[Column Name]]></text>
        </element>
    </columnHeader>

    <!-- 6. Detail band (repeats for each data row) -->
    <detail>
        <band height="20">
            <element kind="textField" x="0" y="0" width="200" height="20">
                <expression><![CDATA[$F{fieldName}]]></expression>
            </element>
        </band>
    </detail>

    <!-- 7. Summary band (after all data) -->
    <summary height="30">
        <element kind="textField" x="0" y="0" width="200" height="20">
            <expression><![CDATA[$V{total}]]></expression>
        </element>
    </summary>
</jasperReport>
```

### Expression Types

| Prefix | Meaning | Example |
|---|---|---|
| `$P{name}` | Parameter (passed from Java) | `$P{invoiceId}` |
| `$F{name}` | Field (from data source row) | `$F{productName}` |
| `$V{name}` | Variable (calculated) | `$V{totalAmount}` |

### Common Element Attributes

| Attribute | Description | Example |
|---|---|---|
| `kind` | Element type | `"textField"`, `"staticText"`, `"image"` |
| `x`, `y` | Position (pixels from left/top) | `x="0" y="0"` |
| `width`, `height` | Size in pixels | `width="200" height="20"` |
| `bold` | Bold text | `bold="true"` |
| `italic` | Italic text | `italic="true"` |
| `fontSize` | Font size | `fontSize="14.0"` |
| `hTextAlign` | Horizontal alignment | `"Left"`, `"Center"`, `"Right"` |
| `pattern` | Number/date format | `pattern="$#,##0.00"` |

### Template Examples

<details>
<summary><b>Simple parameter-only report</b></summary>

```xml
<jasperReport name="simple" language="java" pageWidth="595" pageHeight="842"
    columnWidth="555" leftMargin="20" rightMargin="20"
    topMargin="20" bottomMargin="20"
    whenNoDataType="AllSectionsNoDetail">

    <parameter name="title" class="java.lang.String"/>
    <parameter name="date" class="java.lang.String"/>

    <title height="80">
        <element kind="textField" x="0" y="0" width="555" height="40"
                 fontSize="24.0" bold="true" hTextAlign="Center">
            <expression><![CDATA[$P{title}]]></expression>
        </element>
        <element kind="textField" x="0" y="45" width="555" height="20"
                 hTextAlign="Center">
            <expression><![CDATA["Generated on: " + $P{date}]]></expression>
        </element>
    </title>
</jasperReport>
```

> 💡 Use `whenNoDataType="AllSectionsNoDetail"` when your report doesn't have data rows — otherwise JasperReports shows a blank page.

</details>

<details>
<summary><b>Tabular report with data source</b></summary>

```xml
<jasperReport name="product_list" language="java" pageWidth="595" pageHeight="842"
    columnWidth="555" leftMargin="20" rightMargin="20"
    topMargin="20" bottomMargin="20">

    <parameter name="reportTitle" class="java.lang.String"/>

    <field name="name" class="java.lang.String"/>
    <field name="sku" class="java.lang.String"/>
    <field name="price" class="java.lang.Double"/>
    <field name="stock" class="java.lang.Integer"/>

    <title height="40">
        <element kind="textField" x="0" y="0" width="555" height="30"
                 fontSize="18.0" bold="true" hTextAlign="Center">
            <expression><![CDATA[$P{reportTitle}]]></expression>
        </element>
    </title>

    <columnHeader height="25">
        <element kind="staticText" x="0" y="0" width="180" height="20" bold="true">
            <text><![CDATA[Product Name]]></text>
        </element>
        <element kind="staticText" x="180" y="0" width="100" height="20" bold="true">
            <text><![CDATA[SKU]]></text>
        </element>
        <element kind="staticText" x="280" y="0" width="100" height="20" bold="true"
                 hTextAlign="Right">
            <text><![CDATA[Price]]></text>
        </element>
        <element kind="staticText" x="380" y="0" width="100" height="20" bold="true"
                 hTextAlign="Right">
            <text><![CDATA[Stock]]></text>
        </element>
    </columnHeader>

    <detail>
        <band height="20">
            <element kind="textField" x="0" y="0" width="180" height="20">
                <expression><![CDATA[$F{name}]]></expression>
            </element>
            <element kind="textField" x="180" y="0" width="100" height="20">
                <expression><![CDATA[$F{sku}]]></expression>
            </element>
            <element kind="textField" x="280" y="0" width="100" height="20"
                     hTextAlign="Right" pattern="$#,##0.00">
                <expression><![CDATA[$F{price}]]></expression>
            </element>
            <element kind="textField" x="380" y="0" width="100" height="20"
                     hTextAlign="Right">
                <expression><![CDATA[$F{stock}]]></expression>
            </element>
        </band>
    </detail>
</jasperReport>
```

</details>

<details>
<summary><b>Report with calculated variable (SUM)</b></summary>

```xml
<jasperReport name="order" language="java" pageWidth="595" pageHeight="842"
    columnWidth="555" leftMargin="20" rightMargin="20"
    topMargin="20" bottomMargin="20">

    <field name="item" class="java.lang.String"/>
    <field name="quantity" class="java.lang.Integer"/>
    <field name="unitPrice" class="java.lang.Double"/>
    <field name="lineTotal" class="java.lang.Double"/>

    <!-- Variable MUST come after fields but BEFORE bands -->
    <variable name="grandTotal" calculation="Sum" class="java.lang.Double">
        <expression><![CDATA[$F{lineTotal}]]></expression>
    </variable>

    <detail>
        <band height="20">
            <element kind="textField" x="0" y="0" width="200" height="20">
                <expression><![CDATA[$F{item}]]></expression>
            </element>
            <element kind="textField" x="400" y="0" width="100" height="20"
                     hTextAlign="Right" pattern="$#,##0.00">
                <expression><![CDATA[$F{lineTotal}]]></expression>
            </element>
        </band>
    </detail>

    <summary height="30">
        <element kind="staticText" x="300" y="5" width="100" height="20" bold="true">
            <text><![CDATA[TOTAL:]]></text>
        </element>
        <element kind="textField" x="400" y="5" width="100" height="20"
                 bold="true" hTextAlign="Right" pattern="$#,##0.00">
            <expression><![CDATA[$V{grandTotal}]]></expression>
        </element>
    </summary>
</jasperReport>
```

> ⚠️ **Element ordering matters!** In JR 7.x the order must be: `parameter` → `field` → `variable` → `title` → `columnHeader` → `detail` → `summary`

</details>

### Where to place templates

```
src/main/resources/
  reports/
    invoice.jrxml
    invoice_items.jrxml
    product.jrxml
    order.jrxml
```

Reference them as `"reports/invoice.jrxml"` (classpath-relative).

---

## 6. 🔧 Core Engine

### Overview

The core module has three classes that work together:

| Class | Responsibility |
|---|---|
| `ReportCompiler` | Compiles JRXML → `JasperReport`, caches compiled templates |
| `ReportExecutor` | Fills a compiled template with parameters and data |
| `ReportEngine` | Facade that combines compiler + executor in one call |

### Basic usage

```java
ReportEngine engine = new ReportEngine();  // thread-safe, create once

ReportContext ctx = ReportContext.builder("reports/invoice.jrxml")
        .parameter("invoiceId", 42)
        .build();

JasperPrint print = engine.generateReport(ctx);
```

### With a JDBC connection

When your JRXML has an embedded SQL query, pass a JDBC connection:

```java
ReportContext ctx = ReportContext.builder("reports/order.jrxml")
        .parameter("orderId", 100)
        .connection(dataSource.getConnection())  // JR runs the SQL query
        .build();

JasperPrint print = engine.generateReport(ctx);
```

### With a JRDataSource (in-memory data)

```java
// From a list of Maps
List<Map<String, ?>> rows = List.of(
    Map.of("item", "Widget", "amount", 25.00),
    Map.of("item", "Gadget", "amount", 15.50)
);
JRDataSource ds = new JRMapCollectionDataSource(rows);

// From a list of JavaBeans
List<Product> products = getProducts();
JRDataSource ds = new JRBeanCollectionDataSource(products);

ReportContext ctx = ReportContext.builder("reports/products.jrxml")
        .parameter("title", "Product Catalogue")
        .dataSource(ds)
        .build();
```

### Cache management

The `ReportCompiler` caches compiled templates in a `ConcurrentHashMap`. You can manage it directly:

```java
ReportCompiler compiler = engine.getCompiler();

compiler.cacheSize();                     // number of cached templates
compiler.getCached("reports/old.jrxml");  // null if not cached
compiler.evict("reports/old.jrxml");      // force recompilation next time
compiler.clearCache();                    // clear everything
```

> 💡 **Tip:** The cache is unbounded and lives for the lifetime of the `ReportCompiler`. For most applications this is fine — compiled templates are small (~10-50 KB each).

---

## 7. 📋 Report Registry & Subreports

### Registering reports

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

### Looking up reports

```java
ReportDefinition def = registry.lookup("INV-001");  // throws if not found
ReportDefinition def = registry.find("INV-001");    // returns null

boolean exists = registry.contains("INV-001");
int count = registry.size();
Collection<ReportDefinition> all = registry.getAll();
```

### Subreport resolution

The `SubreportResolver` compiles subreport templates and returns them as a parameter map to inject into the main report:

```java
SubreportResolver resolver = new SubreportResolver(engine.getCompiler());

// Compiles "reports/invoice_items.jrxml" and maps it to "ITEMS_SUBREPORT"
Map<String, Object> subreportParams = resolver.resolveSubreports(def);

// Merge into your context
ReportContext ctx = ReportContext.builder(def.getTemplatePath())
        .parameters(subreportParams)
        .parameter("invoiceId", 42)
        .connection(conn)
        .build();
```

> 💡 **How subreports work:** In JasperReports, a subreport is referenced via a parameter of type `JasperReport`. The `SubreportResolver` compiles each subreport JRXML and puts the compiled object into a parameter map with the matching key.

---

## 8. 📤 Exporting Reports

### Export to PDF, XLSX, CSV

```java
ExportService exportService = new ExportService();

// To byte array
byte[] pdf  = exportService.exportToBytes(print, ExportFormat.PDF);
byte[] xlsx = exportService.exportToBytes(print, ExportFormat.XLSX);
byte[] csv  = exportService.exportToBytes(print, ExportFormat.CSV);

// To output stream (streaming — no buffering in memory)
try (OutputStream out = new FileOutputStream("invoice.pdf")) {
    exportService.export(print, ExportFormat.PDF, out);
}
```

### Paginated export

Export only a specific range of pages:

```java
// Export pages 0-4 (first 5 pages)
byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF, PageRange.of(0, 4));

// Export a single page
byte[] page = exportService.exportToBytes(print, ExportFormat.PDF, PageRange.single(2));

// Export all pages (same as without PageRange)
byte[] all = exportService.exportToBytes(print, ExportFormat.PDF, PageRange.all());
```

### Register a custom exporter

```java
exportService.register(new ReportExporter() {
    @Override
    public void export(JasperPrint print, OutputStream output) {
        // your custom export logic (e.g., HTML, DOCX)
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.PDF; // replaces the default PDF exporter
    }
});
```

---

## 9. 🧩 Composite Reports

Merge multiple filled reports into a single document — useful for financial packages, multi-section reports, etc.

```java
ReportEngine engine = new ReportEngine();

JasperPrint balanceSheet = engine.generateReport(/* ... */);
JasperPrint incomeStmt   = engine.generateReport(/* ... */);
JasperPrint cashFlow     = engine.generateReport(/* ... */);

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

## 10. 🗄️ Metadata-Driven Reports

This is one of the most powerful features — instead of hard-coding report definitions in Java, you store them in database tables. This lets business users or admins configure reports without code changes.

### How it works (flowchart)

```
┌───────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Client   │    │  Metadata    │    │   Query      │    │   Report     │
│  Request  │    │  Loader      │    │  Executor    │    │   Engine     │
│           │    │              │    │              │    │              │
│ code:     │───►│ 1. Load from │───►│ 3. Run SQL   │───►│ 5. Compile   │
│ "INV-001" │    │    database  │    │    with      │    │    JRXML     │
│ params:   │    │              │    │    named     │    │              │
│ {id: 42}  │    │ 2. Validate  │    │    params    │    │ 6. Fill with │
│           │    │    params    │    │              │    │    data      │
└───────────┘    └──────────────┘    │ 4. Convert   │    │              │
                                     │    to        │    │ 7. Return    │
                                     │    JRData    │    │    JasperPrint│
                                     └──────────────┘    └──────────────┘
```

### Database Schema (ER Diagram)

```
┌──────────────────────┐
│       reports         │
├──────────────────────┤
│ code (PK)            │──┐
│ name                 │  │
│ description          │  │
│ template_path        │  │    ┌─────────────────────────┐
│ query                │  │    │   report_parameters      │
│ active               │  │    ├─────────────────────────┤
└──────────────────────┘  ├───►│ id (PK)                 │
                          │    │ report_code (FK)         │
                          │    │ name                     │
                          │    │ type                     │
                          │    │ required                 │
                          │    │ default_value            │
                          │    └─────────────────────────┘
                          │
                          │    ┌─────────────────────────┐
                          │    │   report_subreports      │
                          │    ├─────────────────────────┤
                          ├───►│ id (PK)                 │
                          │    │ report_code (FK)         │
                          │    │ parameter_name           │
                          │    │ template_path            │
                          │    └─────────────────────────┘
                          │
                          │    ┌─────────────────────────┐
                          │    │    report_queries        │
                          │    ├─────────────────────────┤
                          ├───►│ id (PK)                 │
                          │    │ report_code (FK)         │
                          │    │ name                     │
                          │    │ query_text               │
                          │    │ sort_order               │
                          │    └─────────────────────────┘
                          │
                          │    ┌─────────────────────────┐
                          │    │   report_permissions     │
                          │    ├─────────────────────────┤
                          └───►│ id (PK)                 │
                               │ report_code (FK)         │
                               │ role_name                │
                               │ can_view                 │
                               │ can_export               │
                               └─────────────────────────┘
```

### Step 1: Create the tables

Run the DDL from `jasper-framework-metadata/src/main/resources/schema/report-metadata.sql`:

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
    report_code   VARCHAR(50) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(100) NOT NULL DEFAULT 'java.lang.String',
    required      BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(500),
    CONSTRAINT fk_param_report FOREIGN KEY (report_code) REFERENCES reports(code)
);

CREATE TABLE report_subreports (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code     VARCHAR(50) NOT NULL,
    parameter_name  VARCHAR(100) NOT NULL,
    template_path   VARCHAR(500) NOT NULL,
    CONSTRAINT fk_sub_report FOREIGN KEY (report_code) REFERENCES reports(code)
);

CREATE TABLE report_queries (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code   VARCHAR(50) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    query_text    TEXT NOT NULL,
    sort_order    INT DEFAULT 0,
    CONSTRAINT fk_query_report FOREIGN KEY (report_code) REFERENCES reports(code)
);

CREATE TABLE report_permissions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code   VARCHAR(50) NOT NULL,
    role_name     VARCHAR(100) NOT NULL,
    can_view      BOOLEAN DEFAULT TRUE,
    can_export    BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_perm_report FOREIGN KEY (report_code) REFERENCES reports(code)
);
```

### Step 2: Insert report metadata

```sql
-- Register the invoice report
INSERT INTO reports (code, name, description, template_path, query, active)
VALUES ('INV-001', 'Invoice Report', 'Customer invoice with line items',
        'reports/invoice.jrxml',
        'SELECT item_name AS item, quantity, unit_price AS unitPrice,
                (quantity * unit_price) AS lineTotal
         FROM invoice_items WHERE invoice_id = :invoiceId',
        true);

-- Define parameters
INSERT INTO report_parameters (report_code, name, type, required, default_value)
VALUES ('INV-001', 'invoiceId', 'java.lang.Long', true, null),
       ('INV-001', 'companyName', 'java.lang.String', false, 'Our Company');

-- Set permissions
INSERT INTO report_permissions (report_code, role_name, can_view, can_export)
VALUES ('INV-001', 'ROLE_ADMIN', true, true),
       ('INV-001', 'ROLE_ACCOUNTANT', true, true),
       ('INV-001', 'ROLE_VIEWER', true, false);
```

### Step 3: Generate from metadata

```java
// Create the service (uses plain JDBC — no Spring required)
ReportMetadataService service = new ReportMetadataService(
        dataSource, engine, engine.getCompiler());

// This single call does everything:
// 1. Loads metadata from DB for "INV-001"
// 2. Validates that required params are present
// 3. Applies default values for missing optional params
// 4. Runs the SQL query with :invoiceId = 42
// 5. Converts SQL results to JRDataSource
// 6. Resolves any subreports
// 7. Compiles and fills the report
JasperPrint print = service.generateFromMetadata("INV-001",
        Map.of("invoiceId", 42L));

// Export as usual
byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF);
```

### Named parameter support

The `QueryExecutor` supports `:paramName` syntax in SQL queries:

```sql
SELECT * FROM orders
WHERE customer_id = :customerId
  AND status = :status
  AND created_at >= :startDate
```

Parameters are bound by name from the map you pass to `generateFromMetadata()`.

```java
JasperPrint print = service.generateFromMetadata("ORD-001", Map.of(
    "customerId", 42L,
    "status", "SHIPPED",
    "startDate", LocalDate.of(2024, 1, 1)
));
```

### Loading metadata directly

If you need to inspect metadata before generating:

```java
ReportMetadataLoader loader = new ReportMetadataLoader(dataSource);

// Load a single report (with parameters and subreports)
ReportMetadata meta = loader.load("INV-001");
String template = meta.getTemplatePath();
String query = meta.getQuery();
List<ReportParameterMetadata> params = meta.getParameters();
List<ReportSubreportMetadata> subs = meta.getSubreports();

// Load all active reports (summary only, no params/subreports)
List<ReportMetadata> allActive = loader.loadAllActive();
```

### When to use metadata-driven vs hard-coded

| Scenario | Approach |
|---|---|
| Fixed reports that never change | Hard-coded with `ReportContext.builder()` |
| Reports configurable by business users | Metadata-driven |
| Multi-tenant apps with per-tenant reports | Metadata-driven (one DB per tenant, or tenant column) |
| Rapid prototyping | Hard-coded first, migrate to metadata later |
| Reports with complex Java logic for data | Hard-coded (build data source in Java, not SQL) |

### Real-world patterns

**Multi-tenant reports:** Add a `tenant_id` column to the `reports` table. Filter by tenant when loading metadata.

**User-configurable reports:** Build an admin UI that reads/writes the metadata tables. Users can change SQL queries, add parameters, and toggle reports on/off without deployments.

**Report permissions:** Query the `report_permissions` table to check if the current user's role has `can_view` / `can_export` before generating.

---

## 11. ⚡ Async Report Generation

Run reports in the background when they take too long for synchronous requests.

```java
ReportEngine engine = new ReportEngine();
ExportService exportService = new ExportService();
ReportWorker worker = new ReportWorker(engine, exportService);

// Create a 4-thread pool (daemon threads)
try (ReportJobService jobService = new ReportJobService(worker, 4)) {

    // Submit a job — returns immediately
    ReportJob job = jobService.submit("INV-001", "reports/invoice.jrxml",
            ExportFormat.PDF, Map.of("invoiceId", 42));

    System.out.println("Job ID: " + job.getId());       // UUID
    System.out.println("Status: " + job.getStatus());    // PENDING or RUNNING

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

} // close() shuts down the pool gracefully (waits up to 60 seconds)
```

### Job lifecycle

```
PENDING ──► RUNNING ──► COMPLETED  (result bytes available)
                    └──► FAILED     (errorMessage available)
```

---

## 12. 💾 Report Storage

Store and retrieve generated report files using a pluggable storage API.

### File system storage

```java
ReportStorageService storage = new FileSystemStorageService(Paths.get("/var/reports"));

// Store
storage.store("invoices/2024/INV-001.pdf", pdfBytes);

// Retrieve
try (InputStream in = storage.retrieve("invoices/2024/INV-001.pdf")) {
    // stream to HTTP response, copy to another location, etc.
}

// Check existence
boolean exists = storage.exists("invoices/2024/INV-001.pdf");

// Delete
storage.delete("invoices/2024/INV-001.pdf");
```

> 🔒 **Security:** `FileSystemStorageService` includes path traversal protection — keys like `../../etc/passwd` are rejected with a `StorageException`.

### Implementing custom storage (S3/MinIO)

Implement the `ReportStorageService` interface:

```java
public class S3StorageService implements ReportStorageService {
    private final S3Client s3;
    private final String bucket;

    @Override
    public void store(String key, byte[] content) {
        s3.putObject(PutObjectRequest.builder()
            .bucket(bucket).key(key).build(),
            RequestBody.fromBytes(content));
    }

    @Override
    public InputStream retrieve(String key) {
        return s3.getObject(GetObjectRequest.builder()
            .bucket(bucket).key(key).build());
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket).key(key).build());
    }

    @Override
    public boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder()
                .bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
```

---

## 13. 🚄 Performance Features

### Virtualizer support (for large reports)

When a report generates thousands of pages, all pages are held in memory. Virtualizers swap pages to disk to reduce memory usage.

```java
// File-based: swap pages to individual temp files
VirtualizerConfig config = VirtualizerConfig.file(20);  // keep 20 pages in memory

// Swap-file: single swap file with block allocation (best I/O performance)
VirtualizerConfig config = VirtualizerConfig.swapFile(20);

// GZIP: compress pages in memory (no disk I/O, but still limited by heap)
VirtualizerConfig config = VirtualizerConfig.gzip(50);

// Use in context
ReportContext ctx = ReportContext.builder("reports/huge_report.jrxml")
        .connection(conn)
        .virtualizer(config)  // ← inject virtualizer
        .build();

JasperPrint print = engine.generateReport(ctx);
```

**When to use which:**

| Virtualizer | Best for | Trade-off |
|---|---|---|
| `file(n)` | General use, simple setup | One file per page = many file handles |
| `swapFile(n)` | High-volume production | Best I/O, slightly more complex |
| `gzip(n)` | When disk access is not possible | Pages compressed in heap — still memory-bound |

### Pagination

Export only the pages you need:

```java
// First 10 pages
byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF, PageRange.of(0, 9));

// Single page preview
byte[] preview = exportService.exportToBytes(print, ExportFormat.PDF, PageRange.single(0));
```

---

## 14. 🍃 Spring Boot Integration

### Setup

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-spring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Auto-registered beans

All `@ConditionalOnMissingBean` — you can override any of them:

- `ReportCompiler`
- `ReportExecutor`
- `ReportEngine`
- `ReportRegistry`
- `SubreportResolver`
- `ExportService`
- `JasperFrameworkProperties`

### Built-in REST endpoints

When Spring Web is on the classpath, two endpoints are auto-registered:

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/reports/{code}?format=PDF` | Generate report by code |
| `GET` | `/reports/job/{id}` | Check async job status |

```bash
# Generate invoice as PDF
curl -X POST "http://localhost:8080/reports/invoice?format=PDF&invoiceId=42" -o invoice.pdf

# Check job status
curl http://localhost:8080/reports/job/abc-123-def
```

### Configuration

```yaml
jasper:
  framework:
    template-prefix: reports/       # classpath prefix for templates
    cache-enabled: true             # enable template caching
    async-thread-pool-size: 8       # thread pool for async jobs
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

## 15. ☕ Jakarta EE / CDI Integration

### Setup

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Auto-produced beans (via CDI `@Produces`)

All `@ApplicationScoped`:
- `ReportCompiler`, `ReportExecutor`, `ReportEngine`
- `ReportRegistry`, `SubreportResolver`, `ExportService`

### JSF backing bean example

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
        HttpServletResponse response = (HttpServletResponse)
            fc.getExternalContext().getResponse();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
        response.getOutputStream().write(pdf);
        fc.responseComplete();
    }
}
```

---

## 16. 🔨 Maven Plugin

Pre-compile `.jrxml` to `.jasper` during the build to catch template errors early.

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
                <sourceDirectory>${project.basedir}/src/main/resources/reports</sourceDirectory>
                <outputDirectory>${project.build.outputDirectory}/reports</outputDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**What it does:**
1. Scans `sourceDirectory` for `*.jrxml` files
2. Compiles each to `*.jasper` binary in `outputDirectory`
3. Runs during the `process-resources` phase
4. **Fails the build** if any template has errors

---

## 17. 🔗 Module Dependency Graph

```
jasper-framework-core  (pure Java + JasperReports + SLF4J)
  │
  ├── jasper-framework-registry       (+ core)
  ├── jasper-framework-exporter       (+ core + jasperreports-pdf + jasperreports-excel-poi)
  │
  ├── jasper-framework-metadata       (+ core + registry)
  ├── jasper-framework-composition    (+ core + exporter)
  ├── jasper-framework-async          (+ core + exporter + registry)
  │
  ├── jasper-framework-spring         (+ core + registry + exporter; Spring = provided)
  ├── jasper-framework-jakarta        (+ core + registry + exporter; CDI = provided)
  │
  ├── jasper-framework-storage        (standalone; SLF4J only)
  └── jasper-framework-maven-plugin   (standalone; Maven Plugin API + JasperReports)
```

**Design rules:**
- `core` has **zero** framework dependencies — only JasperReports + SLF4J
- `spring` and `jakarta` use `provided` scope — your app supplies the runtime
- SLF4J is the logging facade — **no logging implementation is shipped**

---

## 18. 📚 API Reference

### `com.jasperframework.core`

| Class | Key Methods |
|---|---|
| `ReportEngine` | `generateReport(ReportContext)`, `getCompiler()`, `getExecutor()` |
| `ReportCompiler` | `compile(path)`, `getCached(path)`, `evict(path)`, `clearCache()`, `cacheSize()` |
| `ReportExecutor` | `execute(ReportTemplate, ReportContext)` |
| `ReportContext.Builder` | `builder(templatePath)`, `parameter(k,v)`, `parameters(Map)`, `dataSource(JRDataSource)`, `connection(Connection)`, `virtualizer(VirtualizerConfig)`, `build()` |
| `VirtualizerConfig` | `file(maxPages)`, `swapFile(maxPages)`, `gzip(maxPages)` |
| `VirtualizerFactory` | `create(VirtualizerConfig)` → `JRVirtualizer` |
| `ExportFormat` | `PDF`, `XLSX`, `CSV` |

### `com.jasperframework.registry`

| Class | Key Methods |
|---|---|
| `ReportRegistry` | `register(def)`, `lookup(code)`, `find(code)`, `contains(code)`, `unregister(code)`, `getAll()`, `size()`, `clear()` |
| `ReportDefinition.Builder` | `builder(code, templatePath)`, `displayName()`, `description()`, `format()`, `subreport(param, path)`, `build()` |
| `SubreportResolver` | `resolveSubreports(ReportDefinition)` → `Map<String, Object>` |

### `com.jasperframework.exporter`

| Class | Key Methods |
|---|---|
| `ExportService` | `export(print, format, outputStream)`, `exportToBytes(print, format)`, `export(print, format, output, pageRange)`, `exportToBytes(print, format, pageRange)`, `register(exporter)`, `supportsFormat(format)` |
| `ReportExporter` | `export(JasperPrint, OutputStream)`, `getFormat()` |
| `PageRange` | `of(start, end)`, `single(page)`, `all()`, `isAll()` |

### `com.jasperframework.metadata`

| Class | Key Methods |
|---|---|
| `ReportMetadataService` | `generateFromMetadata(reportCode, params)` → `JasperPrint` |
| `ReportMetadataLoader` | `load(reportCode)` → `ReportMetadata`, `loadAllActive()` → `List` |
| `QueryExecutor` | `execute(sql, params)` → `JRDataSource` |
| `ReportMetadata` | `getCode()`, `getTemplatePath()`, `getQuery()`, `getParameters()`, `getSubreports()`, `isActive()` |

### `com.jasperframework.composition`

| Class | Key Methods |
|---|---|
| `ReportComposer` | `compose(name, JasperPrint...)`, `compose(name, List<JasperPrint>)` |
| `ReportMergeService` | `mergeAndExport(name, format, reports...)` → `byte[]` |

### `com.jasperframework.async`

| Class | Key Methods |
|---|---|
| `ReportJobService` | `submit(code, path, format, params)` → `ReportJob`, `getJob(id)`, `getAllJobs()`, `close()` |
| `ReportJob` | `getId()`, `getStatus()`, `getResult()`, `getErrorMessage()`, `getCreatedAt()`, `getCompletedAt()` |
| `JobStatus` | `PENDING`, `RUNNING`, `COMPLETED`, `FAILED` |

### `com.jasperframework.storage`

| Class | Key Methods |
|---|---|
| `ReportStorageService` | `store(key, bytes)`, `retrieve(key)` → `InputStream`, `delete(key)`, `exists(key)` |
| `FileSystemStorageService` | Constructor takes `Path rootDir`. `getRootDir()` returns configured path. |

---

## 19. ⚠️ Exception Hierarchy

```
RuntimeException
  └── ReportException                   (base for all framework errors)
        ├── ReportCompilationException  (JRXML not found or invalid)
        ├── ReportExecutionException    (report filling failed)
        └── ReportExportException       (export failed)

  └── StorageException                  (storage operations failed)
```

| Exception | When thrown | Typical cause |
|---|---|---|
| `ReportCompilationException` | `ReportCompiler.compile()` | JRXML not on classpath, or invalid JRXML syntax |
| `ReportExecutionException` | `ReportExecutor.execute()` | Missing required parameter, null data source when expected |
| `ReportExportException` | `ExportService.export()` | Invalid page range, export library error |
| `StorageException` | `ReportStorageService.*` | File I/O error, key not found, path traversal |
| `ReportException` | Various | Generic — catch this to handle all framework errors |

**Catching strategy:**

```java
try {
    JasperPrint print = engine.generateReport(ctx);
    byte[] pdf = exportService.exportToBytes(print, ExportFormat.PDF);
} catch (ReportCompilationException e) {
    // Template issue — check JRXML path and format
} catch (ReportExecutionException e) {
    // Data issue — check parameters, data source, SQL query
} catch (ReportExportException e) {
    // Export issue — check page range, disk space
} catch (ReportException e) {
    // Catch-all for any framework error
}
```

---

## 20. 🔍 Troubleshooting & FAQ

### Common Errors

<details>
<summary><b>❌ "JRXML template not found on classpath: reports/invoice.jrxml"</b></summary>

**Cause:** The `.jrxml` file is not in the right location.

**Fix:**
1. Ensure the file is at `src/main/resources/reports/invoice.jrxml`
2. Verify it's included in the build output: check `target/classes/reports/invoice.jrxml`
3. The path is case-sensitive
4. Don't use leading slash: use `"reports/invoice.jrxml"`, not `"/reports/invoice.jrxml"`

</details>

<details>
<summary><b>❌ XML parsing error / "Unrecognized element" when compiling JRXML</b></summary>

**Cause:** You're using JasperReports 6.x JRXML format with a 7.x library.

**Fix:** Convert your template to 7.x format:
- Remove XML namespace declarations
- Change `<title><band height="50">` to `<title height="50">`
- Change `<textField><reportElement x="0".../>` to `<element kind="textField" x="0"...>`
- Change `<textFieldExpression>` to `<expression>`

See [Section 5](#5--jrxml-template-format-jr-7x) for the full comparison.

</details>

<details>
<summary><b>❌ Report generates a blank page</b></summary>

**Cause:** The report has no data rows and `whenNoDataType` is not set.

**Fix:** Add `whenNoDataType="AllSectionsNoDetail"` to the `<jasperReport>` root element:

```xml
<jasperReport name="my-report" ... whenNoDataType="AllSectionsNoDetail">
```

</details>

<details>
<summary><b>❌ "Required parameter missing: invoiceId"</b></summary>

**Cause:** The metadata defines `invoiceId` as required, but you didn't pass it.

**Fix:** Include all required parameters in the map:

```java
service.generateFromMetadata("INV-001", Map.of("invoiceId", 42L));
```

</details>

<details>
<summary><b>❌ OutOfMemoryError with large reports</b></summary>

**Cause:** Too many pages held in memory.

**Fix:** Use a virtualizer:

```java
ReportContext ctx = ReportContext.builder("reports/huge.jrxml")
        .virtualizer(VirtualizerConfig.swapFile(20))
        .connection(conn)
        .build();
```

See [Section 13](#13--performance-features) for details.

</details>

<details>
<summary><b>❌ Font not found / missing font error</b></summary>

**Cause:** JasperReports can't find the font used in the template.

**Fix:**
1. Use standard fonts (`Helvetica`, `Courier`, `Times-Roman`) which are built into PDF
2. Or add font JARs to your classpath (e.g., `jasperreports-fonts`)
3. In JRXML, avoid specifying fonts explicitly — let JR use defaults

</details>

---

## 21. ✅ Best Practices

### Thread Safety

- `ReportEngine`, `ReportCompiler`, `ExportService`, and `ReportRegistry` are all **thread-safe** — create one instance and share across threads
- `ReportContext` is **immutable** — safe to pass across threads
- `ReportJobService` is thread-safe with its internal `ConcurrentHashMap`

### Caching

- The template cache is **unbounded** — compiled templates are small (~10-50 KB), so this is fine for most apps
- Call `compiler.evict(path)` if you update a template at runtime
- In development, you may want to call `compiler.clearCache()` on each request for hot-reload

### Error Handling

- Catch `ReportException` (the base class) for general error handling
- Use specific subclasses (`ReportCompilationException`, `ReportExecutionException`, `ReportExportException`) when you need to handle them differently
- Always log the full exception — `ReportException` wraps the original JasperReports `JRException`

### Testing Reports

- Use JUnit 5 + AssertJ for all tests
- Put test JRXML files in `src/test/resources/reports/`
- For export tests, verify the output is non-empty and has correct magic bytes (e.g., `%PDF` for PDF)
- Use H2 in-memory database for metadata tests
- Use Awaitility (not `Thread.sleep`) for async job tests

### Logging

- The framework uses SLF4J — **no logging implementation is shipped**
- Add `slf4j-simple` (test scope) or `logback-classic` (production) to your app
- Key log messages:
  - `INFO` — template compiled, report filled, job completed
  - `DEBUG` — cache hits, parameter details, SQL execution
  - `ERROR` — compilation/execution/export failures

### Project Organization

- Keep JRXML templates in `src/main/resources/reports/`
- Use meaningful report codes: `INV-001`, `ORD-002`, `FIN-BALANCE-SHEET`
- Group templates by domain: `reports/invoices/`, `reports/orders/`, `reports/finance/`
- One JRXML per file — don't try to combine unrelated reports
