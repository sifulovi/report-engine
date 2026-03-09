You are a senior Java architect designing an **enterprise-grade JasperReports framework** that will be used as a **reusable plugin/library** in any Java ecosystem such as:

* Spring Boot
* Jakarta EE / JSF
* Grails
* Micronaut
* Quarkus
* Plain Java applications

The goal is to build a **metadata-driven JasperReports engine** similar to reporting engines used in ERP/banking systems.

The framework must be packaged as **Maven/Gradle modules** so that any application can simply add it as a dependency.

Your task is to guide the development **step-by-step** and generate the full code progressively.

Do NOT jump to the final solution immediately.
Instead build the framework in **clear incremental steps**.

---

# Core Requirements

The framework must support:

1. Dynamic report registry
2. JRXML compilation with caching
3. Subreport support
4. Dynamic parameters
5. Dynamic SQL execution
6. Multiple exporters (PDF, XLSX, CSV)
7. Composite report merging
8. Async report generation
9. Metadata-driven report configuration from database
10. Compatible with Spring Boot, Jakarta EE, and other Java apps
11. Can be distributed as Maven artifacts
12. Optional Maven plugin to precompile reports

---

# Step 1 — Project Structure

First design a **multi-module Maven project**.

Modules should include:

parent-project

jasper-framework-core
ReportEngine
ReportCompiler
ReportExecutor

jasper-framework-registry
ReportDefinition
ReportRegistry

jasper-framework-exporter
PDF exporter
XLSX exporter
CSV exporter

jasper-framework-metadata
database driven report definitions
report parameters
report queries

jasper-framework-composition
report merge engine
composite reports

jasper-framework-async
background report jobs

jasper-framework-spring
spring boot autoconfiguration

jasper-framework-jakarta
CDI beans for Jakarta EE

jasper-framework-maven-plugin
compile JRXML during build

Show the **complete directory structure**.

Then generate the **parent pom.xml**.

---

# Step 2 — Core Report Engine

Implement the core engine.

Classes:

ReportEngine
ReportCompiler
ReportExecutor

Requirements:

* JRXML compilation with caching
* Thread safe
* Load templates from classpath
* Allow parameter injection

Provide full Java code.

---

# Step 3 — Report Registry

Implement a registry that manages all reports.

Classes:

ReportDefinition
ReportRegistry

Features:

* register report
* lookup by code
* support multiple export formats
* support subreport definitions

Show code and usage examples.

---

# Step 4 — Subreport Support

Add subreport support.

Requirements:

* resolve subreports dynamically
* inject compiled subreports via parameters
* avoid file-path coupling

Create:

SubreportResolver

Explain how main reports reference subreports.

---

# Step 5 — Export Layer

Implement exporters.

Supported formats:

PDF
XLSX
CSV

Design:

ExportService
Exporter interface
PdfExporter
XlsxExporter
CsvExporter

The engine must select exporters dynamically.

---

# Step 6 — Composite Reports

Implement a system to merge multiple reports.

Example:

Financial Report
Balance Sheet
Income Statement
Cash Flow

Design classes:

ReportComposer
ReportMergeService

Merge **JasperPrint pages** before exporting.

---

# Step 7 — Metadata Driven Reports

Now implement a **database-driven reporting system**.

Create database schema:

reports
report_parameters
report_queries
report_subreports
report_permissions

Explain schema.

Create metadata loader service:

ReportMetadataService

Execution flow:

load metadata
validate parameters
execute SQL
convert result to JRDataSource
fill report

Provide Java code.

---

# Step 8 — Dynamic SQL Execution

Implement SQL execution engine.

Requirements:

* NamedParameterJdbcTemplate support
* map results to JRMapCollectionDataSource
* support parameter binding

Provide code.

---

# Step 9 — Async Report Jobs

Implement background report generation.

Database table:

report_jobs

Fields:

id
report_code
status
created_at
file_path

Statuses:

PENDING
RUNNING
COMPLETED
FAILED

Implement:

ReportJobService
ReportWorker

Support large reports.

---

# Step 10 — Storage Layer

Implement storage abstraction.

Storage types:

File system
S3
MinIO

Create interface:

ReportStorageService

Provide file storage implementation.

---

# Step 11 — Spring Boot Integration

Create Spring Boot autoconfiguration.

Features:

* auto register ReportEngine
* configuration via application.yml
* REST API example

Endpoints:

POST /reports/{code}
GET /reports/job/{id}

---

# Step 12 — Jakarta EE / JSF Integration

Provide CDI producers so the engine works inside:

Jakarta EE
JSF
PrimeFaces

Example usage inside a JSF backing bean.

---

# Step 13 — Maven Plugin

Create a Maven plugin:

jasper:compile

Purpose:

Compile all JRXML files during build.

Classes:

CompileMojo

Explain plugin usage.

---

# Step 14 — Performance Features

Add:

Compiled template cache
JRVirtualizer support
Streaming exporters
Pagination support

Explain when to use them.

---

# Step 15 — Example Application

Create an example project using Spring Boot.

Example reports:

invoice
product
order_with_item

Show:

JRXML
Java usage
API usage

---

# Output Rules

For each step:

1. Explain architecture
2. Provide full code
3. Provide directory structure
4. Show usage example

Do not skip steps.

Wait for confirmation before continuing to the next step.

Build the framework progressively like a real enterprise project.
