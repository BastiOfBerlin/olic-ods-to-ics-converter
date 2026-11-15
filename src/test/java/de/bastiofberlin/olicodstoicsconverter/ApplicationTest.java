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
    cli = new CommandLine(new Application());
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

    assertThat(exitCode).isNotZero();
    assertThat(output).doesNotExist();
  }
}
