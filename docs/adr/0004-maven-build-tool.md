# ADR-0004: Maven as the backend build tool

- **Status:** Accepted
- **Date:** 2026-06-14
- **Phase:** 1

## Context

The tech spec lists Spring Boot but does not mandate a build tool. Both Maven and
Gradle are first-class for Spring Boot. The local machine has neither Maven nor
Gradle installed (only JDK 25), so the chosen tool must work without a global
install.

## Decision

Use **Maven** with the **Maven Wrapper** (`mvnw` / `mvnw.cmd`) committed to the
repo. The wrapper bootstraps the correct Maven version on first run, so no global
Maven install is required.

## Consequences

- ✅ Most widely recognized build tool for Spring Boot — lowest friction for
  reviewers/recruiters reading the project.
- ✅ Maven Wrapper means `./mvnw spring-boot:run` works on a clean machine.
- ✅ Declarative `pom.xml` is easy to scan for the dependency list.
- ⚠️ More verbose than Gradle Kotlin DSL → acceptable; clarity is favored here.
- ↩️ Reversible: switching to Gradle later affects only the build files, not
  application code.
