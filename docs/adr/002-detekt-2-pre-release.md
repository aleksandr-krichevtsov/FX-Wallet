# ADR-002: detekt 2.0 pre-release

- **Status:** Accepted
- **Date:** 2026-10-14
- **Plan references:** section 5 (code quality), section 8 (Definition of Done)

## Context

The plan requires detekt with no errors and a green CI. The toolchain is AGP 9.4.1 with built-in Kotlin, Kotlin 2.4.20, Gradle 9.6.0, Gradle daemon on JDK 25.

## Decision

Use detekt `2.0.0-alpha.6` (plugin id `dev.detekt`) in every module, with a shared `../../config/detekt/detekt.yml` applied on top of the default config (`buildUponDefaultConfig = true`).

Facts (detekt README compatibility table; detekt 2.0 migration guide):

- `2.0.0-alpha.6` is built with Gradle 9.6.1, Kotlin 2.4.10, AGP 9.3.1, JDK max 25.
- The 2.0 Gradle plugin compiles against the AGP API only, so it works with AGP 9 built-in Kotlin.

## Alternatives considered

- *detekt 1.23.8 (latest stable).* Rejected: JDK max 21 while our Gradle daemon runs on 25; built against Kotlin 2.0.21; no stated support for AGP 9 built-in Kotlin; its docs are marked as no longer actively maintained.
- *detekt's ktlint wrapper instead of ktlint-gradle.* Not chosen: ktlint-gradle already covers formatting; running ktlint twice adds nothing.
- *No detekt.* Rejected: required by the Definition of Done.

## Consequences

- (+) Matches our Gradle, Kotlin, AGP and JDK versions.
- (−) Pre-release: rule ids and config keys may change before 2.0.0.
- Revisit when detekt 2.0.0 stable ships: upgrade, run `./gradlew detektGenerateConfig`, and diff against our config, as the migration guide describes.
