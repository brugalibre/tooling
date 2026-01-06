package ch.brugalibre.tooling.log.analyzer;

import ch.brugalibre.tooling.log.analyzer.extract.ErrorType;
import ch.brugalibre.tooling.log.analyzer.extract.LogLineExtractor;
import ch.brugalibre.tooling.log.analyzer.group.LogLineGrouper;
import ch.brugalibre.tooling.log.analyzer.model.LogAnalyzeResult;
import ch.brugalibre.tooling.log.analyzer.publish.LogAnalyzeResultFileExporter;
import ch.brugalibre.tooling.log.analyzer.publish.LogAnalyzeResultPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

class LogAnalyzerTest {

    private static final Pattern IGNORE_PATTERN = Pattern.compile("(.*)Pappedipupedi(.*)");
    private static final String IST_DETAIL_FILE_NAME = "Detail-RechMcd-Processing-analyzed-error.txt";
    private static final String SOLL_DETAIL_FILE_NAME = "Soll-Detail-RechMcd-Processing-analyzed-error.txt";
    private static final String IST_SUMMARY_FILE_NAME = "Summary-RechMcd-Processing-analyzed-error.txt";
    private static final String SOLL_SUMMARY_FILE_NAME = "Soll-Summary-RechMcd-Processing-analyzed-error.txt";
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path PATH_IST_DETAIL_FILE = Paths.get("src", "test", "resources", IST_DETAIL_FILE_NAME);
    private static final Path PATH_IST_SUMMARY_FILE = Paths.get("src", "test", "resources", IST_SUMMARY_FILE_NAME);
    private static final Path PATH_SOLL_DETAIL_FILE = Paths.get("src", "test", "resources", SOLL_DETAIL_FILE_NAME);
    private static final Path PATH_SOLL_SUMMARY_FILE = Paths.get("src", "test", "resources", SOLL_SUMMARY_FILE_NAME);

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(PATH_IST_DETAIL_FILE);
        Files.deleteIfExists(PATH_IST_SUMMARY_FILE);
    }

    @Test
    void analyzeLogFilesForAllModes() throws IOException {
        // Given
        LogAnalyzer logAnalyzer = new LogAnalyzer(new LogLineExtractor(logLine -> "RechnungMigrationDataflow"), new LogLineGrouper());
        String path = RESOURCE_DIRECTORY.toAbsolutePath().toString();
        LogAnalyzeResultPublisher resultPublisher = new LogAnalyzeResultFileExporter();

        // When
        List<LogAnalyzeResult> logAnalyzeResults = logAnalyzer.analyzeLogFilesForAllModes(path, ErrorType.RECH_MCD_PROCESSING, List.of(IGNORE_PATTERN));

        // Then
        logAnalyzeResults.forEach(logAnalyzeResult -> {
            try {
                resultPublisher.publish(logAnalyzeResult);
            } catch (IOException e) {
                fail(e);
            }
        });
        Assertions.assertEquals(-1, Files.mismatch(PATH_SOLL_DETAIL_FILE, PATH_IST_DETAIL_FILE));
        Assertions.assertEquals(-1, Files.mismatch(PATH_SOLL_SUMMARY_FILE, PATH_IST_SUMMARY_FILE));
    }
}