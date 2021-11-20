package formatters;

import play.data.format.Formatters;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InstantSimpleFormatter extends Formatters.SimpleFormatter<Instant> {
    private static final DateTimeFormatter instantFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withZone(ZoneId.from(ZoneOffset.UTC));

    @Override
    public Instant parse(String text, Locale locale) throws ParseException {
        return Instant.parse(text);
    }

    @Override
    public String print(Instant instant, Locale locale) {
        return instantFormatter.format(instant);
    }
}
