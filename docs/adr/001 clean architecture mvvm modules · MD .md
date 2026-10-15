# ADR-001: Clean Architecture + MVVM, module structure and boundary enforcement

- **Status:** Accepted
- **Date:** 2026-10-07
- **Plan references:** section 5.1, section 2.1, section 8

## Context

FX Wallet must show a correct, interview-defensible Android architecture (plan sections 1 and 5). Two forces apply:

1. MVVM is the presentation pattern recommended in the [Guide to app architecture](https://developer.android.com/topic/architecture). Clean Architecture (Robert C. Martin) describes the structure of the whole app. They don't conflict: in Clean Architecture terms the ViewModel is an interface adapter between the UI and the use cases.
2. Plan section 5.1 requires layer boundaries to be **enforced by the compiler, not by agreement**. Two points of the original plan didn't meet that:
    - `:app` depended on `:data` "for DI only", but `implementation(project(":data"))` exposes every `public` class of `:data` to `:app`.
    - `:domain` could depend only on `kotlinx-coroutines-core`, so use cases couldn't declare `@Inject constructor`.

## Decision

### Module structure

| Module | Type | Contains | Depends on |
|---|---|---|---|
| `:domain` | Pure Kotlin/JVM | Entities, value objects, use cases, repository/service interfaces, domain errors | Only `kotlinx-coroutines-core` and `javax.inject` |
| `:data` | Android library | `internal` implementations: repositories, Room, Retrofit, DataStore, WorkManager (Worker, scheduler), mappers; Hilt modules with bindings (`di` package) | `:domain` |
| `:designsystem` | Android library | Theme, tokens, Compose components | Nothing in the project |
| `:app` | Android application | Compose screens, ViewModels, UI models, navigation, `@HiltAndroidApp` Application (Hilt composition root, `HiltWorkerFactory` configuration); no binding modules | `:domain`, `:data` (Hilt graph aggregation only), `:designsystem` |

The presentation layer uses MVVM + UDF: a single `StateFlow<UiState>`, events as a `sealed interface`, one-off effects via `Channel`. ViewModels call only use cases.

### D1: `:data` hides its implementations; Hilt bindings live in `:data`

- Every implementation type in `:data` is `internal`: repository implementations, `WorkManagerRatesSyncScheduler`, `RatesSyncWorker`, Room Entities/DAOs/Database, Retrofit DTOs/API, mappers. Kotlin `internal` = visible only inside the same Gradle module ([kotlinlang.org: visibility modifiers](https://kotlinlang.org/docs/visibility-modifiers.html)).
- Hilt modules binding domain interfaces to these implementations live in `:data/di`. Pattern verified in Now in Android, commit `a49ed253` on `main`, file `core/data/src/main/kotlin/com/google/samples/apps/nowinandroid/core/data/di/DataModule.kt`:
    - the module class is `public` (`abstract class DataModule`);
    - each `@Binds` method is `internal abstract fun`;
    - the bound implementations are `internal class ... @Inject constructor` (e.g. `OfflineFirstTopicsRepository`).
- `:app` still depends on `:data`, only so Hilt can aggregate the graph at the application root.
- Periodic sync goes through the domain: `RatesSyncScheduler.schedulePeriodicSync()` + `SchedulePeriodicRatesSyncUseCase`, called from `Application.onCreate()`.
- `HiltWorkerFactory` / `Configuration.Provider` setup stays in the `Application` in `:app`. `HiltWorkerFactory` is a public library class, so this doesn't conflict with D1.
- **Dispatchers and `Clock`:** the `@Provides` dispatchers module (`Dispatchers.IO`, `Dispatchers.Default`) and the `Clock` binding live in `:data/di`, so all bindings are in one place. Classes inject `CoroutineDispatcher` directly, as in Android's [coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices) ("Inject Dispatchers").
- **`Clock` is `kotlin.time.Clock` (stdlib), not a custom domain interface:** [What's new in Kotlin 2.3.0](https://kotlinlang.org/docs/whatsnew23.html) stabilizes `kotlin.time.Clock` and `kotlin.time.Instant`, so the project requires Kotlin 2.3.0 or later. The binding is a `@Provides` returning `Clock.System` in `:data/di`; no custom interface or implementation class. The [`Clock` API docs](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.time/-clock/) recommend passing a `Clock` explicitly instead of calling `Clock.System` directly, which matches plan principle 8.
- **Dispatcher qualifiers live in `:domain`:** `@IoDispatcher` and `@DefaultDispatcher` are declared in `:domain`, built on `javax.inject.Qualifier` (allowed by D2). A qualifier must be visible at every injection site, and `:domain` doesn't depend on `:data`. Use cases may need one: the [domain layer guide](https://developer.android.com/topic/architecture/domain-layer) says use cases must be main-safe and are responsible for moving long-running blocking work to the appropriate thread.

**Alternatives considered:**
- *Bindings in `:app`, enforced by detekt `ForbiddenImport`.* Rejected: a lint rule, not compiler enforcement; contradicts section 5.1.
- *Separate `:data-api` / `:data-impl` modules.* Not chosen: more modules and build setup for no extra guarantee over `internal` in a single `:data` module.
- *Dispatcher qualifiers `internal` in `:data` + a rule "use cases never take dispatchers".* Rejected: adds a constraint the domain layer guide doesn't require.
- *A separate `:common` module for qualifiers and the dispatchers module, as in Now in Android (`core/common`).* Rejected: a fifth module for two annotations is overkill at this scope.
- *A `DispatcherProvider` interface.* Rejected: Android's coroutines best practices show injecting `CoroutineDispatcher` directly.
- *A custom `Clock` interface in `:domain` with an `internal` system implementation in `:data`.* Rejected: duplicates a stable stdlib type and adds code we'd have to own.
- *`java.time.Clock` (JDK).* Not chosen: one time API end-to-end, but less idiomatic Kotlin and more verbose `Duration` arithmetic.

**Consequences:**
- (+) A ViewModel that imports `RatesRepositoryImpl` or a Room Entity fails to compile.
- (+) Hilt tests in `:app` can swap the data layer with `@TestInstallIn(replaces = [DataModule::class])`, which needs only the module class to be public. NiA does this in `core/data-test/.../TestDataModule.kt`.
- (−) `:app` tests can't use real `:data` implementations; they use fakes of domain interfaces (preferred anyway, see the Testing section of the project skill).
- (−) Hilt modules in `:data` mean `:data` depends on Hilt. It already did via Hilt Work (section 2.1).
- (−) `kotlin.time` has no calendar date type. Day grouping (F6.1) and the Frankfurter rate date use `java.time.LocalDate` (JDK, API 26+), converting with `Instant.toJavaInstant()` at that one boundary. `kotlinx-datetime` would avoid the second time API but is a new `:domain` dependency, which D2 doesn't allow.
- (?) NiA's Room DAOs and Entities are `public` because they live in a separate `core/database` module; NiA's Room Database (`NiaDatabase`) and `@HiltWorker` (`SyncWorker`) are `internal`. `internal` DAOs and Entities in our single `:data` module are **not confirmed by a reference project**; verify on the first Room build in week 2.

### D2: `javax.inject` is allowed in `:domain`

- `:domain` may depend on `javax.inject` (JSR-330). It is the only exception to `kotlinx-coroutines-core` being `:domain`'s only dependency.
- Use cases declare `@Inject constructor`. Now in Android does the same in `core/domain` (`implementation(libs.javax.inject)` in `core/domain/build.gradle.kts`; `class GetFollowableTopicsUseCase @Inject constructor`). Note: NiA's `core/domain` is an Android library that depends on `core:data`, so it's a reference for the annotation only, not for the module setup.

**Rationale:**
- JSR-330 is a standard annotation set, not a framework: no Android, no Hilt. The annotations do nothing without a DI container.
- `:domain` still compiles and runs its tests on the plain JVM.

**Alternatives considered:**
- *No `javax.inject`; a `@Provides` method in a Hilt module for every use case.* Rejected: 10+ boilerplate methods that must change whenever a use case's constructor changes.

**Consequences:**
- (+) Adding a use case needs no DI code.
- (−) Purist counter-argument: annotations in the domain are a leak of an outer concern into the core (the dependency rule says the inner circle shouldn't mention outer-circle names). Accepted trade-off: the annotation is a standard, behaviour-free marker; the domain stays framework-free and JVM-testable, and we avoid the boilerplate above.

## Enforcement

- Compiler: module dependencies and `internal` visibility.
- `:domain` build file: only `kotlinx-coroutines-core` and `javax.inject`.
- Code review checklist (`android-mentor` skill) and Definition of Done (plan section 8).
