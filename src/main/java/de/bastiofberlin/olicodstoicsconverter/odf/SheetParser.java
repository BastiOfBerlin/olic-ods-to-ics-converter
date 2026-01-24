package de.bastiofberlin.olicodstoicsconverter.odf;

import de.bastiofberlin.olicodstoicsconverter.odf.model.Art;
import de.bastiofberlin.olicodstoicsconverter.odf.model.ColumnType;
import de.bastiofberlin.olicodstoicsconverter.odf.model.Termin;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;

@UtilityClass
@Log
public class SheetParser {
  private final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY);
  private final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY);

  public List<Termin> readTermine(final Path odsPath) throws Exception {
    final var ret = new ArrayList<Termin>();
    try (final var doc = OdfSpreadsheetDocument.loadDocument(odsPath.toFile())) {
      final var table = doc.getTableList(false).getFirst();
      Map<ColumnType, Integer> columnIndices = null;
      int emptyRows = 0;
      for (int rowIdx = 0; ; rowIdx++) {
        if (rowIdx % 10 == 0) {
          final int finalRowIdx = rowIdx;
          log.info(() -> "Parsing row %d...".formatted(finalRowIdx));
        }
        final var row = table.getRowByIndex(rowIdx);
        if (row.getOdfElement().getTableNumberRowsRepeatedAttribute() > 1 || emptyRows > 2) {
          break;
        }
        final var firstCellValue = row.getCellByIndex(0).getStringValue();
        if (null == firstCellValue || firstCellValue.isEmpty()) {
          emptyRows++;
          continue;
        }
        if (ColumnType.DATUM.getValue().equals(firstCellValue)) {
          columnIndices = getColumnIndices(row);
          continue;
        }
        Objects.requireNonNull(columnIndices, "Could not determine column indices.");
        try {
          if ("".equals(row.getCellByIndex(columnIndices.get(ColumnType.ART)).getStringValue())) {
            continue;
          }
          final var termin = parseRow(row, columnIndices);
          termin.ifPresent(ret::add);
        } catch (final Exception e) {
          throw new RuntimeException("Error while parsing row " + rowIdx, e);
        }
      }
    }
    return ret;
  }

  private Optional<Termin> parseRow(
      final OdfTableRow row, final Map<ColumnType, Integer> columnIndices) {
    final var termin = Termin.builder();
    for (final Map.Entry<ColumnType, Integer> entry : columnIndices.entrySet()) {
      final Integer cellIdx = entry.getValue();
      final ColumnType columnType = entry.getKey();
      final var cell = row.getCellByIndex(cellIdx);
      final var cellValue = cell.getStringValue();
      switch (columnType) {
        case DATUM -> termin.datum(LocalDate.parse(cellValue, DATE_FORMATTER));
        case ART -> termin.art(Art.parse(cellValue));
        case THEMA -> termin.thema(cellValue);
        case START -> termin.start(LocalTime.parse(cellValue, TIME_FORMATTER));
        case ENDE -> termin.ende(LocalTime.parse(cellValue, TIME_FORMATTER));
      }
    }
    return Optional.of(termin.build());
  }

  private Map<ColumnType, Integer> getColumnIndices(final OdfTableRow row) {
    final var ret = new EnumMap<ColumnType, Integer>(ColumnType.class);
    for (int cellIdx = 0; ; cellIdx++) {
      final var cell = row.getCellByIndex(cellIdx);
      if (cell.getOdfElement().getTableNumberColumnsRepeatedAttribute() > 3) {
        break;
      }
      try {
        ret.put(ColumnType.parse(cell.getStringValue()), cellIdx);
      } catch (final IllegalArgumentException e) {
        // do nothing
      }
    }
    if (ret.size() != ColumnType.values().length) {
      throw new RuntimeException("Could not find all Column types! " + ret.values());
    }
    log.info(
        () ->
            "Column indices: "
                + ret.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map((e) -> "[%s:%d]".formatted(e.getKey(), e.getValue()))
                    .collect(Collectors.joining(",")));
    return ret;
  }
}
