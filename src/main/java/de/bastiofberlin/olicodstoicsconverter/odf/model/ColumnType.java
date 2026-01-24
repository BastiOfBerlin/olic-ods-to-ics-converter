package de.bastiofberlin.olicodstoicsconverter.odf.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ColumnType {
  DATUM("Datum"),
  ART("Art"),
  THEMA("Thema"),
  START("Start"),
  ENDE("Ende"),
  ;
  private final String value;

  public static ColumnType parse(final String s) {
    for (final var type : values()) {
      if (type.getValue().equals(s)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ColumnType: " + s);
  }
}
