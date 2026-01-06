package ch.brugalibre.tooling.migration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record MigrationStatusInfo(BigDecimal avTimePerRecordInMs, BigDecimal estimatedTimeForAllInH,
                                  LocalDateTime eta, int bufferInHours) {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm");

    @Override
    public String toString() {
        return "average duration per records: " + avTimePerRecordInMs + "ms" +
                "\nestimated duration to finish migration: " + estimatedTimeForAllInH + "h" +
                "\nsecurity buffer: " + bufferInHours + "h" +
                "\neta: " + DATE_TIME_FORMATTER.format(etaWithBuffer());

    }

    public LocalDateTime etaWithBuffer() {
        return eta.plusHours(bufferInHours);
    }
}
