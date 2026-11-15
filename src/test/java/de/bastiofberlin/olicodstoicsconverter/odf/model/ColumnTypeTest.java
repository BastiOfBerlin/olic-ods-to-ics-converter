package de.bastiofberlin.olicodstoicsconverter.odf.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnTypeTest {

  @ParameterizedTest
  @EnumSource(ColumnType.class)
  void parseRoundTripsEveryHeaderName(final ColumnType type) {
    assertThat(ColumnType.parse(type.getValue())).isSameAs(type);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "Tag", "datum", "DATUM", "Datum "})
  void parseRejectsUnknownHeaderNames(final String value) {
    assertThatThrownBy(() -> ColumnType.parse(value))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown ColumnType");
  }

  @Test
  void headerNamesMatchTheSpreadsheet() {
    assertThat(ColumnType.values())
        .extracting(ColumnType::getValue)
        .containsExactly("Datum", "Art", "Thema", "Start", "Ende");
  }
}
