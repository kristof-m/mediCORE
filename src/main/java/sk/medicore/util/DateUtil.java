package sk.medicore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtil {

    private static final String[] SK_MONTHS = {
        "januára", "februára", "marca", "apríla", "mája", "júna",
        "júla", "augusta", "septembra", "októbra", "novembra", "decembra"
    };

    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter SHORT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT_YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** "Pondelok, 20. apríla 2026 o 10:30" */
    public static String formatDisplay(LocalDateTime dt) {
        return dayName(dt.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("sk")))
            + ", " + dt.getDayOfMonth() + ". " + SK_MONTHS[dt.getMonthValue() - 1]
            + " " + dt.getYear() + " o " + dt.format(TIME_FMT);
    }

    /** "20.04.2026 10:30" */
    public static String formatShort(LocalDateTime dt) {
        return dt.format(SHORT_FMT);
    }

    /** "20. apríla 2026" */
    public static String formatDate(LocalDateTime dt) {
        return dt.getDayOfMonth() + ". " + SK_MONTHS[dt.getMonthValue() - 1] + " " + dt.getYear();
    }

    /** "20. apríla 2026" from LocalDate */
    public static String formatDate(LocalDate d) {
        return d.getDayOfMonth() + ". " + SK_MONTHS[d.getMonthValue() - 1] + " " + d.getYear();
    }

    /** "10:30" */
    public static String formatTime(LocalDateTime dt) {
        return dt.format(TIME_FMT);
    }

    /** "Pondelok, 20. apríla 2026" — for day headings in calendar views */
    public static String formatDayHeading(LocalDate d) {
        return dayName(d.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("sk")))
            + ", " + d.getDayOfMonth() + ". " + SK_MONTHS[d.getMonthValue() - 1] + " " + d.getYear();
    }

    /** DB storage format */
    public static String formatDb(LocalDateTime dt) {
        return dt.format(DATE_FMT_YMD);
    }

    private static String dayName(String raw) {
        return raw.isEmpty() ? raw : raw.substring(0, 1).toUpperCase() + raw.substring(1);
    }
}
