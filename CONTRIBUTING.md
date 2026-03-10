# Contributing to Jasper Framework

Thank you for considering contributing to Jasper Framework! This guide will help you get started.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Coding Standards](#coding-standards)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Reporting Issues](#reporting-issues)
- [Maintainer Guide](#maintainer-guide)

---

## Getting Started

### Prerequisites

- **Java 17+** (JDK, not JRE)
- **Git**
- **Maven 3.9+** (optional — project includes Maven Wrapper)

### Fork & Clone

```bash
# 1. Fork the repository on GitHub

# 2. Clone your fork
git clone https://github.com/<your-username>/report-engine.git
cd report-engine

# 3. Add upstream remote
git remote add upstream https://github.com/<org>/report-engine.git
```

## Development Setup

### Build the Project

```bash
# Full build with tests
./mvnw clean install

# Quick build (skip tests)
./mvnw clean install -DskipTests
```

### Build a Single Module

If you're working on a specific module, build only what you need:

```bash
# Build core and install to local repo
./mvnw clean install -pl jasper-framework-core

# Build a module and all its dependencies
./mvnw clean install -pl jasper-framework-exporter -am
```

### Run Tests

```bash
# All tests
./mvnw test

# Tests for a specific module
./mvnw test -pl jasper-framework-core

# A specific test class
./mvnw test -pl jasper-framework-core -Dtest=ReportCompilerTest

# A specific test method
./mvnw test -pl jasper-framework-core -Dtest=ReportCompilerTest#shouldCompileSimpleReport
```

### IDE Setup

**IntelliJ IDEA** (recommended):
1. Open → select the root `pom.xml` → Open as Project
2. Ensure Project SDK is set to Java 17+
3. Maven auto-import should detect all modules

**Eclipse**:
1. File → Import → Maven → Existing Maven Projects
2. Select the root directory

**VS Code**:
1. Install "Extension Pack for Java" and "Maven for Java"
2. Open the root folder

## Making Changes

### Branch Naming

Create a feature branch from `main`:

```bash
git checkout main
git pull upstream main
git checkout -b <type>/<short-description>
```

Branch name conventions:

| Prefix | Use for |
|---|---|
| `feature/` | New features (e.g., `feature/s3-storage`) |
| `fix/` | Bug fixes (e.g., `fix/pdf-export-encoding`) |
| `refactor/` | Code restructuring (e.g., `refactor/compiler-cache`) |
| `docs/` | Documentation only (e.g., `docs/api-examples`) |
| `test/` | Test improvements (e.g., `test/metadata-integration`) |

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short summary>

<optional body>
```

**Types**: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `chore`

**Scope**: module name without the `jasper-framework-` prefix

Examples:
```
feat(core): add report template caching with TTL
fix(exporter): handle empty pages in PDF export
test(metadata): add integration tests for named parameters
docs(spring): update autoconfiguration examples
build: upgrade JasperReports to 7.1.0
```

## Coding Standards

### General Rules

- **Java 17** — use modern features (records, sealed classes, pattern matching) where appropriate
- **No framework dependencies in `core`** — core must remain pure Java + JasperReports
- **Thread safety** — all shared state must be thread-safe (prefer `ConcurrentHashMap`, immutable objects)
- **Immutable by default** — use Builder pattern for value objects, `Collections.unmodifiable*` for collections

### Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: same-line opening brace (K&R style)
- **Naming**:
  - Classes: `PascalCase` (e.g., `ReportCompiler`)
  - Methods/variables: `camelCase` (e.g., `compileReport`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`)
  - Packages: `com.jasperframework.<module>` (e.g., `com.jasperframework.core`)

### Dependencies

- **Never add version numbers in module POMs** — all versions are centralized in the parent POM's `<dependencyManagement>`
- **Use `provided` scope** for framework-specific dependencies (Spring, Jakarta) in adapter modules
- **No runtime logging binding** — use `slf4j-api` only; `slf4j-simple` is test-scope only

### Testing

- **JUnit 5** + **AssertJ** + **Mockito** for all tests
- Place test JRXML files in `src/test/resources/reports/`
- Use **JasperReports 7.x JRXML format** (no XML namespaces, `<element kind="...">` syntax)
- Integration tests that need a database should use **H2 in-memory**
- Async tests should use **Awaitility** (not `Thread.sleep`)

### JRXML Format (JasperReports 7.x)

All JRXML templates must use the 7.x format. **Do NOT use 6.x format.**

```xml
<!-- CORRECT: 7.x format -->
<jasperReport name="my-report" language="java"
              pageWidth="595" pageHeight="842"
              columnWidth="555" leftMargin="20" rightMargin="20"
              topMargin="20" bottomMargin="20">
    <title height="50">
        <element kind="textField" x="0" y="0" width="200" height="30">
            <expression><![CDATA[$P{title}]]></expression>
        </element>
    </title>
</jasperReport>
```

See [guideline.md](guideline.md) for full JRXML 7.x documentation.

## Submitting a Pull Request

### Before Submitting

1. **Ensure all tests pass**:
   ```bash
   ./mvnw clean verify
   ```

2. **Build the full project** to catch cross-module issues:
   ```bash
   ./mvnw clean install
   ```

3. **Rebase on latest main**:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

### PR Checklist

- [ ] Branch is up to date with `main`
- [ ] All existing tests pass (`./mvnw clean verify`)
- [ ] New code has tests (aim for meaningful coverage, not just line count)
- [ ] No version numbers in module POMs (use parent's `dependencyManagement`)
- [ ] No framework dependencies leaked into `core` module
- [ ] Commit messages follow conventional commit format
- [ ] JRXML files use 7.x format

### PR Description Template

```markdown
## Summary
Brief description of what this PR does.

## Changes
- Bullet points of specific changes

## Testing
How was this tested? What tests were added?

## Related Issues
Closes #<issue-number>
```

### Review Process

1. A maintainer will review your PR
2. Address any feedback by pushing additional commits (don't force-push during review)
3. Once approved, a maintainer will merge using squash-merge

## Reporting Issues

When opening an issue, please include:

- **Module affected** (e.g., `jasper-framework-core`)
- **Java version** and **JasperReports version**
- **Steps to reproduce**
- **Expected vs actual behavior**
- **Stack trace** (if applicable)

---

## Maintainer Guide

### Versioning

This project follows [Semantic Versioning](https://semver.org/):

- **MAJOR** (2.0.0) — breaking API changes
- **MINOR** (1.1.0) — new features, backward compatible
- **PATCH** (1.0.1) — bug fixes, backward compatible

### Updating Dependency Versions

All versions are centralized in the **parent `pom.xml`** `<properties>` section:

```xml
<properties>
    <jasperreports.version>7.0.3</jasperreports.version>
    <spring-boot.version>3.4.1</spring-boot.version>
    <jakarta.cdi.version>4.0.1</jakarta.cdi.version>
    <!-- ... -->
</properties>
```

To update a dependency:

1. Change the version in the parent POM's `<properties>`
2. Run `./mvnw clean install` to verify nothing breaks
3. Check for breaking API changes in the dependency's release notes
4. Commit with: `build: upgrade <dependency> to <version>`

To check for available updates:
```bash
./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates
```

### Releasing a New Version

1. **Update version** from SNAPSHOT to release:
   ```bash
   ./mvnw versions:set -DnewVersion=1.0.0
   ./mvnw versions:commit
   ```

2. **Verify the build**:
   ```bash
   ./mvnw clean install
   ```

3. **Commit and tag**:
   ```bash
   git add -A
   git commit -m "release: v1.0.0"
   git tag v1.0.0
   ```

4. **Deploy** (if a Maven repository is configured):
   ```bash
   ./mvnw clean deploy -DskipTests
   ```

5. **Bump to next SNAPSHOT**:
   ```bash
   ./mvnw versions:set -DnewVersion=1.1.0-SNAPSHOT
   ./mvnw versions:commit
   git add -A
   git commit -m "build: bump version to 1.1.0-SNAPSHOT"
   ```

6. **Push**:
   ```bash
   git push origin main --tags
   ```

### Publishing to a Maven Repository

To deploy JARs to a shared Maven repository (Nexus, Artifactory, GitHub Packages), add a `<distributionManagement>` block to the parent POM:

```xml
<distributionManagement>
    <repository>
        <id>releases</id>
        <url>https://your-repo.example.com/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>snapshots</id>
        <url>https://your-repo.example.com/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

Configure credentials in `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>releases</id>
        <username>${env.MAVEN_USERNAME}</username>
        <password>${env.MAVEN_PASSWORD}</password>
    </server>
    <server>
        <id>snapshots</id>
        <username>${env.MAVEN_USERNAME}</username>
        <password>${env.MAVEN_PASSWORD}</password>
    </server>
</servers>
```

Then run: `./mvnw clean deploy`

### Adding a New Module

1. Create the module directory:
   ```bash
   mkdir -p jasper-framework-newmodule/src/main/java/com/jasperframework/newmodule
   mkdir -p jasper-framework-newmodule/src/main/resources
   mkdir -p jasper-framework-newmodule/src/test/java/com/jasperframework/newmodule
   mkdir -p jasper-framework-newmodule/src/test/resources
   ```

2. Create `jasper-framework-newmodule/pom.xml`:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                                http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>

       <parent>
           <groupId>com.jasperframework</groupId>
           <artifactId>jasper-framework</artifactId>
           <version>1.0.0-SNAPSHOT</version>
       </parent>

       <artifactId>jasper-framework-newmodule</artifactId>
       <name>Jasper Framework - New Module</name>

       <dependencies>
           <dependency>
               <groupId>com.jasperframework</groupId>
               <artifactId>jasper-framework-core</artifactId>
           </dependency>
           <!-- Add module-specific deps here (no version numbers!) -->
       </dependencies>
   </project>
   ```

3. Register in parent POM's `<modules>`:
   ```xml
   <module>jasper-framework-newmodule</module>
   ```

4. If other modules will depend on it, add to parent POM's `<dependencyManagement>`:
   ```xml
   <dependency>
       <groupId>com.jasperframework</groupId>
       <artifactId>jasper-framework-newmodule</artifactId>
       <version>${project.version}</version>
   </dependency>
   ```

5. Build and verify: `./mvnw clean install`

### Adding a New Feature to an Existing Module

1. Create a branch: `git checkout -b feature/<description>`
2. Write the implementation in the appropriate module
3. Add tests
4. Update [guideline.md](guideline.md) if the feature adds public API
5. Run `./mvnw clean install` to verify
6. Submit a PR

### Module Dependency Rules

**Strict rules** — violating these breaks the architecture:

- `core` must have **zero framework dependencies** (no Spring, no Jakarta, no Micronaut)
- `spring` and `jakarta` modules use `provided` scope for framework deps
- No circular dependencies between modules
- All new external dependency versions go in the parent POM's `<properties>` + `<dependencyManagement>`

**Dependency graph** (→ means "depends on"):

```
core  (standalone)
├── registry → core
├── exporter → core
├── metadata → core, registry
├── composition → core, exporter
├── async → core, exporter, registry
├── spring → core, registry, exporter
├── jakarta → core, registry, exporter
└── maven-plugin → jasperreports (standalone, build-time only)
```
