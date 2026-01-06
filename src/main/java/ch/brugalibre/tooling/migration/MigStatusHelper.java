package ch.brugalibre.tooling.migration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

import static ch.brugalibre.tooling.migration.MigrationStatusInfo.DATE_TIME_FORMATTER;

public class MigStatusHelper {

    public static final int AVERAGE_MCD_MIG_DURATION = 25;

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: PerformanceHelper <start-time> <totalRechnungenToProcess> <totalMcdToProcess> " +
                    "<processedRechnungen> <processedMcds>");
            return;
        }
        Scanner scanner = new Scanner(System.in);

        LocalDateTime startDate = LocalDateTime.parse(args[0], DATE_TIME_FORMATTER);
        int bufferInHour = Integer.parseInt(args[1]);
        int totalRechnungenToProcess = Integer.parseInt(args[2]);
        int totalMcdToProcess = Integer.parseInt(args[3]);
        ProcessRecordInput processedRechInput = getProcessedRecords(args, 4, "Enter the amount of processed rechnungen:", scanner);
        MigStatusHelper migStatusHelper = new MigStatusHelper();
        MigrationStatusInfo rechMigStatusInfo = migStatusHelper.calculateETA(startDate, totalRechnungenToProcess,
                processedRechInput.processedRecords(), bufferInHour, 0);

        // Mcds (nur Hochrechnung auf Gesamtmenge, keine Berücksichtigung des Startzeitpunkts oder bereits migrierter Mcds)
        MigrationStatusInfo mcdMigStatusInfo = migStatusHelper.calculateETA(rechMigStatusInfo.etaWithBuffer(), totalMcdToProcess, 0, 0, AVERAGE_MCD_MIG_DURATION);
        List<String> migStatusContent = List.of("\n============ Migrationforecast ================\n",
                "\nRech migration info: " + rechMigStatusInfo,
                "\nMcd migration info: " + mcdMigStatusInfo,
                "\n==============================================");
        migStatusContent.forEach(System.err::println);
        Files.write(Path.of("", "migration-forecast-" + System.currentTimeMillis() + ".txt"), migStatusContent, StandardCharsets.UTF_8);
    }

    private static ProcessRecordInput getProcessedRecords(String[] args, int position, String userHint, Scanner scanner) {
        if (args.length > position) {
            return new ProcessRecordInput(Integer.parseInt(args[position]));
        }
        System.out.println(userHint);
        int inputProcessedRechnungen = Integer.parseInt(scanner.nextLine());
        return new ProcessRecordInput(inputProcessedRechnungen);
    }

    public MigrationStatusInfo calculateETA(LocalDateTime startDate, int totalRecordsToProcess, int processedRecords,
                                            int buffernInHour, int avgMigrationDuration) {
        LocalDateTime now = LocalDateTime.now();
        // Date to calculate the estimated time of arrival (ETA)
        LocalDateTime etaCalcDate = now;
        double timePerRecordInMillis = ((double) ChronoUnit.MILLIS.between(startDate, now)) / (double) processedRecords;
        if (Double.isNaN(timePerRecordInMillis) || Double.isInfinite(timePerRecordInMillis)) {
            // startDate is after(now), equal to now, or almost equal (in which case the time per record would be infinite)
            timePerRecordInMillis = avgMigrationDuration;
            etaCalcDate = startDate;
        }
        double timeForAllRecordsInSeconds = (timePerRecordInMillis * (totalRecordsToProcess - processedRecords)) / 1000;
        BigDecimal avTimePerRecordInMs = BigDecimal.valueOf(timePerRecordInMillis).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedTimeForAllInH = BigDecimal.valueOf(timeForAllRecordsInSeconds / 3600).setScale(2, RoundingMode.HALF_UP);
        return new MigrationStatusInfo(avTimePerRecordInMs, estimatedTimeForAllInH, etaCalcDate.plusSeconds((long) timeForAllRecordsInSeconds), totalRecordsToProcess == processedRecords ? 0 : buffernInHour);
    }

    private record ProcessRecordInput (int processedRecords){}
}
