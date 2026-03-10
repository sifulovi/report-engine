# Jasper Framework

Enterprise JasperReports framework — reusable across Spring Boot, Jakarta EE, Micronaut, Quarkus, and plain Java.

## Prerequisites

- **Java 17+** (LTS)
- **Maven 3.9+** (or use the included Maven Wrapper)

## Quick Build

```bash
# Using Maven Wrapper (recommended — no Maven installation needed)
./mvnw clean install

# Or with system Maven
mvn clean install
```

This compiles all 9 modules, runs tests, and installs JARs to your local `~/.m2/repository`.

## Build Commands

| Command | Description |
|---|---|
| `./mvnw clean install` | Full build + tests + install to local repo |
| `./mvnw clean install -DskipTests` | Build without running tests |
| `./mvnw clean package` | Build JARs without installing to local repo |
| `./mvnw clean verify` | Build + run tests (no install) |
| `./mvnw clean install -pl jasper-framework-core` | Build a single module |
| `./mvnw clean install -pl jasper-framework-core -am` | Build a module and its dependencies |
| `./mvnw versions:display-dependency-updates` | Check for dependency updates |

## Artifacts Produced

After `./mvnw clean install`, these JARs are available in your local Maven repository:

| Artifact | Type | Description |
|---|---|---|
| `jasper-framework-core` | JAR | Core engine — compile, fill, execute reports |
| `jasper-framework-registry` | JAR | Report registry and subreport resolution |
| `jasper-framework-exporter` | JAR | PDF, XLSX, CSV export |
| `jasper-framework-metadata` | JAR | Database-driven report definitions + named SQL |
| `jasper-framework-composition` | JAR | Merge multiple reports into one document |
| `jasper-framework-async` | JAR | Background report generation with job tracking |
| `jasper-framework-spring` | JAR | Spring Boot autoconfiguration |
| `jasper-framework-jakarta` | JAR | Jakarta CDI producers |
| `jasper-framework-maven-plugin` | maven-plugin | JRXML build-time compilation |

## Using the JARs in Your Project

After building, add dependencies to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

For Spring Boot projects:
```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-spring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

For Jakarta EE projects:
```xml
<dependency>
    <groupId>com.jasperframework</groupId>
    <artifactId>jasper-framework-jakarta</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

See [guideline.md](guideline.md) for full usage documentation and API reference.

## Project Structure

```
jasper-framework (parent POM)
├── jasper-framework-core          ← Pure Java + JasperReports, zero framework deps
├── jasper-framework-registry      ← Report definitions & subreport resolution
├── jasper-framework-exporter      ← PDF / XLSX / CSV export strategies
├── jasper-framework-metadata      ← JDBC-based report metadata
├── jasper-framework-composition   ← Multi-report merging
├── jasper-framework-async         ← Background job execution
├── jasper-framework-spring        ← Spring Boot autoconfiguration
├── jasper-framework-jakarta       ← Jakarta CDI integration
└── jasper-framework-maven-plugin  ← Build-time JRXML compiler
```

## Documentation

- **[guideline.md](guideline.md)** — Developer usage guide with examples
- **[CONTRIBUTING.md](CONTRIBUTING.md)** — How to contribute
- **[requirements.md](requirements.md)** — Original requirements

## License

TBD
