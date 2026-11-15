package de.bastiofberlin.olicodstoicsconverter.odf.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ArtTest {

  @ParameterizedTest
  @EnumSource(Art.class)
  void parseRoundTripsEveryAbbreviation(final Art art) {
    assertThat(Art.parse(art.getValue())).isSameAs(art);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "Ausbb", "ausb", "BEHL", "Behl "})
  void parseRejectsUnknownAbbreviations(final String value) {
    assertThatThrownBy(() -> Art.parse(value))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown Art");
  }

  @Test
  void onlyZbvBereichsausbildungAndSonstigesAreNotImported() {
    assertThat(Arrays.stream(Art.values()).filter(a -> !a.isDoImport()))
        .containsExactlyInAnyOrder(Art.ZBV, Art.BEREICHSAUSBILDUNG, Art.SONSTIGES);
  }

  @Test
  void abbreviationsAreUnique() {
    assertThat(Arrays.stream(Art.values()).map(Art::getValue)).doesNotHaveDuplicates();
  }
}
