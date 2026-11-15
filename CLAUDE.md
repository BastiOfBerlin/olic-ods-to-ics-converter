# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

CLI tool that converts OLIC (Fachgruppe Ölschaden) duty-roster spreadsheets (`.ods`) into `.ics` calendars. User-facing strings, logs and domain terms (Termin, Art, Thema, Dienstplan) are German.

## Commands

Requires Java 25 (toolchain resolved via foojay; `options.release = 25`).

```shell
./gradlew build                      # compile + test (what CI runs)
./gradlew test                       # JUnit 5 tests
./gradlew test --tests 'FullyQualifiedClass.method'   # single test
./gradlew jar                        # builds build/libs/olic-ods-to-ics-converter-1.0-SNAPSHOT.jar
java -jar <jar> input.ods output.ics
```

Note: the jar is not a fat jar — the `Class-Path` manifest line is commented out in `build.gradle.kts`, so running the plain jar requires dependencies on the classpath.

Tests (JUnit 5 + AssertJ) live in `src/test/java`, mirroring the main packages. `src/test/resources/Dienstplan_2026.ods` is a real sample roster; `src/test/resources/odf/*.ods` are small hand-made fixtures per scenario (LibreOffice-like structure, including a `table:table-column` declaration, which odfdom needs). They are binary; to regenerate them, build `content.xml` + `META-INF/manifest.xml` + an uncompressed `mimetype` entry (first in the zip) per fixture.

## Architecture

Pipeline: `Application` (picocli `Callable`) → `odf.SheetParser.readTermine` → `List<Termin>` → `ical.Writer.writeTermineToFile`.

- `odf/SheetParser` reads the **first sheet** with odfdom. It scans rows until a header row whose first cell is `Datum`, maps header names to column indices (`ColumnType`, all 5 required, so column order is flexible), then parses each following row into a `Termin`. Rows with empty `Art` are skipped; parsing stops at the first blank row after the first data row (the real roster is followed by a sum row, a blank row and a colour legend), after >2 blank rows before the header, or at a repeated-rows element. Dates are `dd.MM.yyyy`, times `HH:mm`. A `Termin` with `Ende` before `Start` is rejected (`IllegalArgumentException`, reported as "Error while parsing row N"); unknown `Art` abbreviations fail fast the same way.
- `odf/model`: `Termin` record (builder), `Art` enum (spreadsheet abbreviation → type, plus a `doImport` flag), `ColumnType` enum (header names). `Column` is currently unused.
- `ical/Writer` builds the calendar with iCal4j (Europe/Berlin timezone). Only `Termin`s whose `Art.isDoImport()` is true are written; event titles are `"Öl: " + label` from a `switch` on `Art`. UIDs are deterministic (`yyyyMMdd'T'HHmm-<ART>@olic-ods-to-ics-converter`) so re-importing a regenerated calendar updates events instead of duplicating them; two events with the same date, start time and `Art` would therefore collide.

Adding a new `Art` requires updating both the enum (abbreviation + `doImport`) and the `switch` in `Writer.getEventName`, which throws `IllegalStateException` for unhandled types (e.g. `ZBV`/`BEREICHSAUSBILDUNG` are non-imported so never reach it).

## Conventions

- Lombok (`@UtilityClass`, `@Builder`, `@Log`, etc.) is used throughout; picocli codegen runs as an annotation processor.
- JSpecify `@NonNull` annotations on models.
- CI (`.github/workflows/gradle.yml`) pins actions by commit SHA with a version comment; Dependabot (`.github/dependabot.yml`) keeps actions and Gradle dependencies updated.
- Dependency versions live in `gradle/libs.versions.toml` (version catalog).
