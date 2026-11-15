package de.bastiofberlin.olicodstoicsconverter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class ApplicationTest {
  @TempDir Path tempDir;

  private final StringWriter out = new StringWriter();
  private final StringWriter err = new StringWriter();
  private CommandLine cli;

  @BeforeEach
  void setUp() {
    cli = Application.createCommandLine();
    cli.setOut(new PrintWriter(out));
    cli.setErr(new PrintWriter(err));
  }

  @Test
  void convertsOdsToIcs() throws Exception {
    final var output = tempDir.resolve("out.ics");

    final int exitCode =
        cli.execute(TestFiles.resource("/odf/basic.ods").toString(), output.toString());

    assertThat(exitCode).isZero();
    assertThat(Files.readString(output))
        .contains("BEGIN:VCALENDAR", "BEGIN:VEVENT", "END:VCALENDAR");
  }

  @Test
  void helpExitsSuccessfully() {
    assertThat(cli.execute("--help")).isZero();
    assertThat(out.toString()).contains("convert");
  }

  @Test
  void missingArgumentsIsAUsageError() {
    assertThat(cli.execute()).isEqualTo(CommandLine.ExitCode.USAGE);
    assertThat(err.toString()).containsIgnoringCase("missing");
  }

  @Test
  void unparsableInputYieldsNonZeroExitCode() {
    final var output = tempDir.resolve("out.ics");

    final int exitCode =
        cli.execute(TestFiles.resource("/odf/unknown_art.ods").toString(), output.toString());

    assertThat(exitCode).isEqualTo(CommandLine.ExitCode.SOFTWARE);
    assertThat(err.toString()).contains("Fehler");
    assertThat(output).doesNotExist();
  }

  @Test
  void missingInputFileIsAUsageError() {
    final int exitCode =
        cli.execute(tempDir.resolve("nope.ods").toString(), tempDir.resolve("o.ics").toString());

    assertThat(exitCode).isEqualTo(CommandLine.ExitCode.USAGE);
    assertThat(err.toString()).contains("nicht gefunden");
  }

  @Test
  void defaultsOutputNextToInput() throws Exception {
    final var input = tempDir.resolve("plan.ods");
    Files.copy(TestFiles.resource("/odf/basic.ods"), input);

    final int exitCode = cli.execute(input.toString());

    assertThat(exitCode).isZero();
    assertThat(tempDir.resolve("plan.ics")).exists();
    assertThat(out.toString()).contains("Termine nach");
  }

  @Test
  void versionIsPrinted() {
    assertThat(cli.execute("--version")).isZero();
    assertThat(out.toString()).contains("olic ods-to-ics-converter");
  }
}
