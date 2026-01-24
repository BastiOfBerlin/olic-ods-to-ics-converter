package de.bastiofberlin.olicodstoicsconverter.odf.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Art {
  BEHLEHRUNG("Behl", true),
  FWB("FwB", true),
  AUSBILDUNG("Ausb", true),
  ZBV("ZBV", false),
  TD("TD", true),
  UEBUNG("Üb", true),
  BEREICHSAUSBILDUNG("BerAusb", false),
  OFFENER_DIENST("OD", true),
  ;

  @Getter private final String value;
  @Getter private final boolean doImport;

  public static Art parse(final String s) {
    for (final var art : values()) {
      if (art.getValue().equals(s)) {
        return art;
      }
    }
    throw new IllegalArgumentException("Unknown Art: " + s);
  }
}
