package de.bastiofberlin.olicodstoicsconverter.odf.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class TerminTest {
  private static final LocalDate DATE = LocalDate.of(2026, 1, 6);

  private static Termin.TerminBuilder builder() {
    return Termin.builder()
        .datum(DATE)
        .art(Art.BEHLEHRUNG)
        .thema("Thema")
        .start(LocalTime.of(19, 0))
        .ende(LocalTime.of(21, 30));
  }

  @Test
  void builderSetsAllFields() {
    final var termin = builder().build();

    assertThat(termin.datum()).isEqualTo(DATE);
    assertThat(termin.art()).isEqualTo(Art.BEHLEHRUNG);
    assertThat(termin.thema()).isEqualTo("Thema");
    assertThat(termin.start()).isEqualTo(LocalTime.of(19, 0));
    assertThat(termin.ende()).isEqualTo(LocalTime.of(21, 30));
  }

  @Test
  void rejectsEndeBeforeStart() {
    assertThatThrownBy(() -> builder().start(LocalTime.of(22, 0)).ende(LocalTime.of(2, 0)).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Ende")
        .hasMessageContaining("Start");
  }

  @Test
  void allowsEndeEqualToStart() {
    assertThatCode(() -> builder().start(LocalTime.NOON).ende(LocalTime.NOON).build())
        .doesNotThrowAnyException();
  }
}
