package de.bastiofberlin.olicodstoicsconverter.odf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import de.bastiofberlin.olicodstoicsconverter.TestFiles;
import de.bastiofberlin.olicodstoicsconverter.odf.model.Art;
import de.bastiofberlin.olicodstoicsconverter.odf.model.Termin;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SheetParserTest {

  private static List<Termin> read(final String resource) throws Exception {
    return SheetParser.readTermine(TestFiles.resource(resource));
  }

  @Test
  void parsesEveryArtAndSkipsRowsWithoutArt() throws Exception {
    final var termine = read("/odf/basic.ods");

    assertThat(termine)
        .extracting(Termin::art)
        .containsExactly(
            Art.BEHLEHRUNG,
            Art.FWB,
            Art.AUSBILDUNG,
            Art.ZBV,
            Art.TD,
            Art.UEBUNG,
            Art.BEREICHSAUSBILDUNG,
            Art.OFFENER_DIENST);
  }

  @Test
  void parsesAllFieldsOfARow() throws Exception {
    final var termine = read("/odf/basic.ods");

    assertThat(termine.getFirst())
        .isEqualTo(
            new Termin(
                LocalDate.of(2026, 1, 6),
                Art.BEHLEHRUNG,
                "Belehrung Thema",
                LocalTime.of(19, 0),
                LocalTime.of(21, 30)));
    assertThat(termine.get(5))
        .isEqualTo(
            new Termin(
                LocalDate.of(2026, 1, 11),
                Art.UEBUNG,
                "Uebung Thema",
                LocalTime.of(8, 0),
                LocalTime.of(18, 0)));
  }

  @Test
  void columnOrderIsFlexibleAndExtraColumnsAreIgnored() throws Exception {
    final var termine = read("/odf/reordered_columns.ods");

    assertThat(termine)
        .containsExactly(
            new Termin(
                LocalDate.of(2026, 1, 6),
                Art.BEHLEHRUNG,
                "Thema X",
                LocalTime.of(19, 0),
                LocalTime.of(21, 30)),
            new Termin(
                LocalDate.of(2026, 1, 20),
                Art.OFFENER_DIENST,
                "offener Dienst",
                LocalTime.of(19, 0),
                LocalTime.of(21, 30)));
  }

  @Test
  void failsWhenARequiredColumnIsMissing() {
    assertThatThrownBy(() -> read("/odf/missing_column.ods"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Could not find all Column types");
  }

  @Test
  void failsWhenDataComesBeforeHeader() {
    assertThatThrownBy(() -> read("/odf/no_header.ods"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Could not determine column indices");
  }

  @Test
  void unknownArtFailsFastWithRowIndex() {
    assertThatThrownBy(() -> read("/odf/unknown_art.ods"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error while parsing row 3")
        .hasRootCauseInstanceOf(IllegalArgumentException.class)
        .rootCause()
        .hasMessageContaining("Unknown Art: Ausbb");
  }

  @Test
  void invalidDateFailsWithRowIndex() {
    assertThatThrownBy(() -> read("/odf/bad_date.ods"))
        .hasMessageContaining("Error while parsing row 3")
        .hasRootCauseInstanceOf(DateTimeParseException.class);
  }

  @Test
  void invalidTimeFailsWithRowIndex() {
    assertThatThrownBy(() -> read("/odf/bad_time.ods"))
        .hasMessageContaining("Error while parsing row 3")
        .hasRootCauseInstanceOf(DateTimeParseException.class);
  }

  @Test
  void endeBeforeStartIsRejected() {
    assertThatThrownBy(() -> read("/odf/ende_before_start.ods"))
        .hasMessageContaining("Error while parsing row 3")
        .hasRootCauseInstanceOf(IllegalArgumentException.class)
        .rootCause()
        .hasMessageContaining("Ende");
  }

  @Test
  void firstBlankRowAfterDataEndsTheRoster() throws Exception {
    // fixture: A, blank, B, 2 blanks, C, 3 blanks, D -> only A is read
    final var termine = read("/odf/gap_rows.ods");

    assertThat(termine).extracting(Termin::thema).containsExactly("A");
  }

  @Test
  void leadingBlankRowsBeforeTheHeaderAreTolerated() throws Exception {
    // every fixture starts with a blank row and a title row before the header
    assertThat(read("/odf/reordered_columns.ods")).hasSize(2);
  }

  @Test
  void headerRowWithoutTrailingFillerCellTerminates() {
    final var termine =
        assertTimeoutPreemptively(
            Duration.ofSeconds(30), () -> read("/odf/header_without_filler.ods"));

    assertThat(termine).hasSize(1);
    assertThat(termine.getFirst().thema()).isEqualTo("Thema");
  }

  @Test
  void parsesTheRealRoster() throws Exception {
    final var termine = SheetParser.readTermine(TestFiles.resource("/Dienstplan_2026.ods"));

    assertThat(termine).isNotEmpty();
    assertThat(termine.getFirst())
        .isEqualTo(
            new Termin(
                LocalDate.of(2026, 1, 6),
                Art.BEHLEHRUNG,
                "Belehrungen UVV, Kraftfahrer, Gefahrstoffe",
                LocalTime.of(19, 0),
                LocalTime.of(21, 30)));
    assertThat(termine.getLast().datum()).isEqualTo(LocalDate.of(2026, 12, 15));
    // SONSTIGES only occurs in later rosters (2027), not in this 2026 sample.
    assertThat(termine)
        .extracting(Termin::art)
        .contains(
            Arrays.stream(Art.values()).filter(a -> a != Art.SONSTIGES).toArray(Art[]::new));
    assertThat(termine)
        .extracting(Termin::datum)
        .isSorted()
        .allSatisfy(d -> assertThat(d.getYear()).isEqualTo(2026));
  }
}
