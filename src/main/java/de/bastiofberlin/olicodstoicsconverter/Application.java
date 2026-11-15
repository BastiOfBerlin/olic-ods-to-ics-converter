package de.bastiofberlin.olicodstoicsconverter;

import de.bastiofberlin.olicodstoicsconverter.ical.Writer;
import de.bastiofberlin.olicodstoicsconverter.odf.SheetParser;
import de.bastiofberlin.olicodstoicsconverter.odf.model.Termin;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@Log
@CommandLine.Command(
    name = "convert",
    mixinStandardHelpOptions = true,
    versionProvider = Application.ManifestVersionProvider.class,
    description = "Wandelt den Dienstplan der Fachgruppe Ölschaden (OLIC) von ODS nach ICS um.")
public class Application implements Callable<Integer> {
  @CommandLine.Spec private CommandSpec spec;

  @CommandLine.Parameters(
      index = "0",
      paramLabel = "<dienstplan.ods>",
      description = "ODS-Tabelle mit dem Dienstplan.")
  private Path inputFile;

  @CommandLine.Parameters(
      index = "1",
      arity = "0..1",
      paramLabel = "<kalender.ics>",
      description = "Ziel-Kalenderdatei (Standard: Eingabedatei mit Endung .ics).")
  private Path outputFile;

  @CommandLine.Option(
      names = {"-v", "--verbose"},
      description = "Ausführliche Ausgabe (inkl. Stacktrace bei Fehlern).")
  private boolean verbose;

  @Override
  public Integer call() throws Exception {
    if (!Files.isRegularFile(inputFile) || !Files.isReadable(inputFile)) {
      throw new CommandLine.ParameterException(
          spec.commandLine(), "Eingabedatei nicht gefunden oder nicht lesbar: " + inputFile);
    }
    if (verbose) {
      // Only raise this application's own loggers; the root level stays INFO so JDK/library
      // internals don't flood the output.
      Logger.getLogger(Application.class.getPackageName()).setLevel(Level.FINE);
      for (final var handler : Logger.getLogger("").getHandlers()) {
        handler.setLevel(Level.FINE);
      }
    }
    final var target = outputFile != null ? outputFile : defaultOutput(inputFile);

    final var termine = SheetParser.readTermine(inputFile);
    log.fine(() -> termine.stream().map(Termin::toString).collect(Collectors.joining("\n")));
    final int count = Writer.writeTermineToFile(target, termine);
    spec.commandLine().getOut().println("%d Termine nach %s geschrieben.".formatted(count, target));
    return CommandLine.ExitCode.OK;
  }

  static Path defaultOutput(final Path input) {
    final var name = input.getFileName().toString();
    final int dot = name.lastIndexOf('.');
    final var base = dot > 0 ? name.substring(0, dot) : name;
    return input.resolveSibling(base + ".ics");
  }

  static CommandLine createCommandLine() {
    final var app = new Application();
    final var cli = new CommandLine(app);
    cli.setExecutionExceptionHandler((ex, commandLine, _) -> {
      final var root = rootCause(ex);
      commandLine.getErr().println("Fehler: %s".formatted(root.getLocalizedMessage()));
      if (app.verbose) {
        ex.printStackTrace(commandLine.getErr());
      }
      commandLine.getErr().flush();
      return CommandLine.ExitCode.SOFTWARE;
    });
    return cli;
  }

  private static Throwable rootCause(final Throwable t) {
    var cur = t;
    while (cur.getCause() != null && cur.getCause() != cur) {
      cur = cur.getCause();
    }
    return cur;
  }

  static void main(final String... args) {
    System.exit(createCommandLine().execute(args));
  }

  static class ManifestVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
      final var version = Application.class.getPackage().getImplementationVersion();
      return new String[] {"olic ods-to-ics-converter " + (version != null ? version : "dev")};
    }
  }
}
