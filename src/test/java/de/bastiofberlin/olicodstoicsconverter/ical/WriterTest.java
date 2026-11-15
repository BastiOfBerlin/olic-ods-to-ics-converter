package de.bastiofberlin.olicodstoicsconverter.ical;

import static org.assertj.core.api.Assertions.assertThat;

import de.bastiofberlin.olicodstoicsconverter.odf.model.Art;
import de.bastiofberlin.olicodstoicsconverter.odf.model.Termin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WriterTest {
  @TempDir Path tempDir;

  private static Termin termin(final Art art, final LocalDate date, final String thema) {
    return new Termin(date, art, thema, LocalTime.of(19, 0), LocalTime.of(21, 30));
  }

  private Path write(final List<Termin> termine) throws IOException {
    final var out = tempDir.resolve("out-" + System.nanoTime() + ".ics");
    Writer.writeTermineToFile(out, termine);
    return out;
  }

  private static Calendar parse(final Path ics) throws IOException, ParserException {
    try (final var in = Files.newInputStream(ics)) {
      return new CalendarBuilder().build(in);
    }
  }

  private static List<VEvent> events(final Calendar calendar) {
    return calendar.getComponents(Component.VEVENT);
  }

  private static String summary(final VEvent event) {
    return event.getSummary().getValue();
  }

  private static String uid(final VEvent event) {
    return event.getUid().orElseThrow().getValue();
  }

  @Test
  void emptyInputProducesValidCalendarWithoutEvents() throws Exception {
    final var calendar = parse(write(List.of()));

    assertThat(events(calendar)).isEmpty();
  }

  @Test
  void writesProdId() throws Exception {
    final var content = Files.readString(write(List.of()));

    assertThat(content)
        .contains("BEGIN:VCALENDAR")
        .contains("PRODID:-//OLIC ODS-to-ICS Converter//iCal4j 1.0//DE");
  }

  @Test
  void onlyImportableArtsBecomeEvents() throws Exception {
    final var termine =
        Arrays.stream(Art.values())
            .map(art -> termin(art, LocalDate.of(2026, 1, 1).plusDays(art.ordinal()), "x"))
            .toList();
    final long importable = Arrays.stream(Art.values()).filter(Art::isDoImport).count();

    final var events = events(parse(write(termine)));

    assertThat(events).hasSize((int) importable);
    assertThat(events)
        .extracting(WriterTest::summary)
        .doesNotContain("Öl: ZBV", "Öl: BerAusb");
  }

  @ParameterizedTest
  @CsvSource({
    "BEHLEHRUNG, Öl: Belehrung",
    "FWB, Öl: FwB",
    "AUSBILDUNG, Öl: Ausbildung",
    "TD, Öl: TD",
    "UEBUNG, Öl: Übung",
    "OFFENER_DIENST, Öl: Offener Dienst"
  })
  void eventSummaryDependsOnArt(final Art art, final String expectedSummary) throws Exception {
    final var events = events(parse(write(List.of(termin(art, LocalDate.of(2026, 1, 6), "x")))));

    assertThat(events).singleElement().extracting(WriterTest::summary).isEqualTo(expectedSummary);
  }

  @Test
  void eventCarriesThemaAsDescription() throws Exception {
    final var thema = "Belehrungen UVV, Kraftfahrer, Gefahrstoffe";
    final var events =
        events(parse(write(List.of(termin(Art.BEHLEHRUNG, LocalDate.of(2026, 1, 6), thema)))));

    assertThat(events.getFirst().getDescription().getValue()).isEqualTo(thema);
  }

  @Test
  void startAndEndAreWrittenInBerlinTime() throws Exception {
    // January = CET (UTC+1), July = CEST (UTC+2): local wall-clock time must be preserved
    final var content =
        Files.readString(
            write(
                List.of(
                    termin(Art.BEHLEHRUNG, LocalDate.of(2026, 1, 6), "winter"),
                    termin(Art.BEHLEHRUNG, LocalDate.of(2026, 7, 7), "sommer"))));

    assertThat(content)
        .contains("DTSTART;TZID=Europe/Berlin:20260106T190000")
        .contains("DTEND;TZID=Europe/Berlin:20260106T213000")
        .contains("DTSTART;TZID=Europe/Berlin:20260707T190000")
        .contains("DTEND;TZID=Europe/Berlin:20260707T213000");
  }

  @Test
  void uidsAreStableAcrossRuns() throws Exception {
    final var termine =
        List.of(
            termin(Art.BEHLEHRUNG, LocalDate.of(2026, 1, 6), "a"),
            termin(Art.OFFENER_DIENST, LocalDate.of(2026, 1, 13), "b"));

    final var first = events(parse(write(termine))).stream().map(WriterTest::uid).toList();
    final var second = events(parse(write(termine))).stream().map(WriterTest::uid).toList();

    assertThat(first).hasSize(2).containsExactlyElementsOf(second);
  }

  @Test
  void uidsAreUniquePerEvent() throws Exception {
    final var termine =
        List.of(
            termin(Art.BEHLEHRUNG, LocalDate.of(2026, 1, 6), "a"),
            termin(Art.BEHLEHRUNG, LocalDate.of(2026, 1, 13), "b"),
            termin(Art.OFFENER_DIENST, LocalDate.of(2026, 1, 13), "c"));

    final var events = events(parse(write(termine)));

    assertThat(events).extracting(WriterTest::uid).hasSize(3).doesNotHaveDuplicates();
  }
}
