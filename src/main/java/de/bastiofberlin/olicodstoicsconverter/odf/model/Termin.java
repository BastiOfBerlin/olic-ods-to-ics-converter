package de.bastiofberlin.olicodstoicsconverter.odf.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record Termin(
    @NonNull LocalDate datum,
    @NonNull Art art,
    @NonNull String thema,
    @NonNull LocalTime start,
    @NonNull LocalTime ende) {

  public Termin {
    if (ende.isBefore(start)) {
      throw new IllegalArgumentException(
          "Ende (%s) liegt vor Start (%s) am %s.".formatted(ende, start, datum));
    }
  }
}
