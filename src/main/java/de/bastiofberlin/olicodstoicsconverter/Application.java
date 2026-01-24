package de.bastiofberlin.olicodstoicsconverter;

import de.bastiofberlin.olicodstoicsconverter.ical.Writer;
import de.bastiofberlin.olicodstoicsconverter.odf.SheetParser;
import de.bastiofberlin.olicodstoicsconverter.odf.model.Termin;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import picocli.CommandLine;

@Log
@CommandLine.Command(
    name = "convert",
    mixinStandardHelpOptions = true,
    version = "olic ods-to-ics-converter 1.0",
    description = "Wandelt den Dienstplan der Fachgruppe Ölschaden (OLIC) von ODS nach ICS um.")
public class Application implements Callable<Integer> {
  @CommandLine.Parameters(
      index = "0",
      description = "ODF spreadsheet Datei (*.ods) mit dem Dienstplan.")
  private Path inputFile;

  @CommandLine.Parameters(index = "1", description = "Kalenderdatei (*.ics).")
  private Path outputFile;

  @Override
  public Integer call() throws Exception {
    final var termine = SheetParser.readTermine(inputFile);
    log.info(() -> termine.stream().map(Termin::toString).collect(Collectors.joining("\n")));
    Writer.writeTermineToFile(outputFile, termine);
    return 0;
  }

  static void main(final String... args) {
    try {
      final int exitCode = new CommandLine(new Application()).execute(args);
      System.exit(exitCode);
    } catch (final Exception e) {
      log.severe("Fehler: %s".formatted(e.getLocalizedMessage()));
      System.exit(-1);
    }
  }
}
