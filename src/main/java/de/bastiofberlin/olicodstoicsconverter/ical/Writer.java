package de.bastiofberlin.olicodstoicsconverter.ical;

import de.bastiofberlin.olicodstoicsconverter.odf.model.Termin;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.FluentCalendar;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Comment;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.jspecify.annotations.NonNull;

@Log
@UtilityClass
public class Writer {
  private static final String PROD_ID = "-//OLIC ODS-to-ICS Converter//iCal4j 1.0//DE";
  private static final String TZ_BERLIN = "Europe/Berlin";
  private static final String TERMIN_PREFIX = "Öl: ";
  private static final ZoneId ZONE = ZoneId.of(TZ_BERLIN);
  private static final VTimeZone V_TIME_ZONE =
      TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone(TZ_BERLIN).getVTimeZone();
  private static final RandomUidGenerator UID_GENERATOR = new RandomUidGenerator();

  public void writeTermineToFile(final Path outputPath, final Collection<Termin> termine)
      throws IOException {
    final var calendar = createCalendar();

    var count = 0;
    for (final var termin : termine) {
      if (!termin.art().isDoImport()) {
        continue;
      }
      addTerminToCalendar(termin, calendar);
      count++;
    }

    logEventCount(count);
    writeCalendarToFile(outputPath, calendar);
  }

  private static void addTerminToCalendar(final Termin termin, final FluentCalendar calendar) {
    final var start = getDateTime(termin.datum(), termin.start());
    final var ende = getDateTime(termin.datum(), termin.ende());
    final var eventName = getEventName(termin);
    final var meeting =
        new VEvent(start, ende, eventName)
            .withProperty(V_TIME_ZONE.getTimeZoneId())
            .withProperty(UID_GENERATOR.generateUid())
            .withProperty(new Description(termin.thema()));
    calendar.withComponent((VEvent) meeting.getFluentTarget());
  }

  private static @NonNull ZonedDateTime getDateTime(final LocalDate date, final LocalTime time) {
    return date.atTime(time).atZone(ZONE);
  }

  private static @NonNull String getEventName(final Termin termin) {
    return switch (termin.art()) {
      case BEHLEHRUNG -> addPrefix("Belehrung");
      case FWB -> addPrefix("FwB");
      case AUSBILDUNG -> addPrefix("Ausbildung");
      case TD -> addPrefix("TD");
      case UEBUNG -> addPrefix("Übung");
      case OFFENER_DIENST -> addPrefix("Offener Dienst");
      default -> throw new IllegalStateException("Unexpected value: " + termin.art());
    };
  }

  private static @NonNull String addPrefix(final String s) {
    return "%s%s".formatted(TERMIN_PREFIX, s);
  }

  private FluentCalendar createCalendar() {
    return new Calendar()
        .withProdId(PROD_ID)
        .withDefaults()
        .withProperty(new Comment("Created: %tc".formatted(ZonedDateTime.now())));
  }

  private void writeCalendarToFile(final Path outputPath, final FluentCalendar calendar)
      throws IOException {
    try (final FileOutputStream fout = new FileOutputStream(outputPath.toFile())) {
      final CalendarOutputter outputter = new CalendarOutputter();
      outputter.output(calendar.getFluentTarget(), fout);
    }
  }

  private static void logEventCount(final int count) {
    log.info(() -> "Writing %d events to calendar...".formatted(count));
  }
}
