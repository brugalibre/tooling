package ch.brugalibre.tooling.log.analyzer;

import ch.brugalibre.tooling.cli.ArgumentHelper;
import ch.brugalibre.tooling.cli.CliUtil;
import ch.brugalibre.tooling.log.analyzer.extract.ErrorType;
import ch.brugalibre.tooling.log.analyzer.extract.LogLineExtractor;
import ch.brugalibre.tooling.log.analyzer.group.GroupMode;
import ch.brugalibre.tooling.log.analyzer.group.LogLineGrouper;
import ch.brugalibre.tooling.log.analyzer.model.LogAnalyzeResult;
import ch.brugalibre.tooling.log.analyzer.model.LogLineDetail;
import ch.brugalibre.tooling.log.analyzer.publish.LogAnalyzeResultFileExporter;
import ch.brugalibre.tooling.log.analyzer.publish.LogAnalyzeResultPrinter;
import ch.brugalibre.tooling.log.analyzer.publish.LogAnalyzeResultPublishDecorator;
import ch.brugalibre.tooling.log.analyzer.publish.LogAnalyzeResultPublisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link LogAnalyzer} orchestrates the log analysis process including reading, extracting and grouping relevant log lines.
 * It creates an instance of {@link LogLineExtractor} which extracts log lines from a certain path. After that, the {@link LogLineGrouper}
 * groups and sorts the extracted log lines based on their error group.
 */
public class LogAnalyzer {

    private final LogLineExtractor logLineExtractor;
    private final LogLineGrouper logLineGrouper;

    public LogAnalyzer(LogLineExtractor logLineExtractor, LogLineGrouper logLineGrouper) {
        this.logLineExtractor = logLineExtractor;
        this.logLineGrouper = logLineGrouper;
    }

    public static void main(String[] args) throws IOException {
        System.out.println();
        ErrorType errorType = ArgumentHelper.getValueForArg(args, "errorType", ErrorType::valueOf);
        if (errorType == null) {
            System.err.println("Error: errorType is required");
            System.err.println("Usage: java LogAnalyzer --errorType <errorType> --groupMode <groupMode> " +
                    "--errorTypeFilter <errorTypeFilter>");
            return;
        }
        String groupModeAsString = ArgumentHelper.getValueForArg(args, "groupMode");
        List<Pattern> filteredErrorTypes = ArgumentHelper.getValuesForArg(args, "errorTypeFilter", Pattern::compile);
        String path = CliUtil.readValueFromSystemIn("Enter path to the log files directory:");
        Function<String, String> dataflowOriginSupplier = getThreadDescriptionSupplier();
        LogAnalyzer logAnalyzer = new LogAnalyzer(new LogLineExtractor(dataflowOriginSupplier), new LogLineGrouper());
        LogAnalyzeResultPublisher logAnalyzeResultPublisher = new LogAnalyzeResultPublishDecorator(List.of(new LogAnalyzeResultFileExporter(), new LogAnalyzeResultPrinter()));
        if (groupModeAsString != null) {
            GroupMode groupMode = GroupMode.valueOf(groupModeAsString);
            LogAnalyzeResult logAnalyzeResult = logAnalyzer.analyzeLogFiles(path, errorType, groupMode, filteredErrorTypes);
            logAnalyzeResultPublisher.publish(logAnalyzeResult);
        } else {
            List<LogAnalyzeResult> logAnalyzeResults = logAnalyzer.analyzeLogFilesForAllModes(path, errorType, filteredErrorTypes);
            for (LogAnalyzeResult logAnalyzeResult : logAnalyzeResults) {
                logAnalyzeResultPublisher.publish(logAnalyzeResult);
            }
        }
    }

    /**
     * Analyzes log files in the specified path for a given error type and group mode.
     *
     * @param path            the path to the directory containing log files
     * @param errorType       the type of error to analyze (e.g., {@link ErrorType#ERRORS}, {@link ErrorType#RECH_MCD_PROCESSING})
     * @param groupMode       the mode of analysis (e.g., DETAIL, SUMMARY)
     * @param excludedPattern a list of patterns to exclude from the analysis
     * @return a {@link LogAnalyzeResult} containing the grouped log lines
     * @throws IOException if an I/O error occurs while reading the log files
     */
    public LogAnalyzeResult analyzeLogFiles(String path, ErrorType errorType, GroupMode groupMode,
                                            List<Pattern> excludedPattern) throws IOException {
        return analyzeLogFiles(path, errorType, List.of(groupMode), excludedPattern).get(0);
    }

    /**
     * Analyzes log files in the specified path for a given error type and for all {@link GroupMode}s.
     *
     * @param path            the path to the directory containing log files
     * @param errorType       the type of error to analyze (e.g., {@link ErrorType#ERRORS}, {@link ErrorType#RECH_MCD_PROCESSING})
     * @param excludedPattern a list of patterns to exclude from the analysis
     * @return a {@link LogAnalyzeResult} containing the grouped log lines
     * @throws IOException if an I/O error occurs while reading the log files
     */
    public List<LogAnalyzeResult> analyzeLogFilesForAllModes(String path, ErrorType errorType, List<Pattern> excludedPattern) throws IOException {
        return analyzeLogFiles(path, errorType, Arrays.asList(GroupMode.values()), excludedPattern);
    }

    private List<LogAnalyzeResult> analyzeLogFiles(String path, ErrorType errorType, List<GroupMode> groupModes,
                                                   List<Pattern> excludedPattern) throws IOException {
        List<LogAnalyzeResult> logAnalyzeResults = new ArrayList<>();
        List<LogLineDetail> extractedLogLines = logLineExtractor.extractLogLinesFromDirectory(path, errorType, excludedPattern);
        for (GroupMode groupMode : groupModes) {
            List<LogLineDetail> groupedLogLines = logLineGrouper.groupLogLineDetails(extractedLogLines, groupMode);
            logAnalyzeResults.add(new LogAnalyzeResult(groupedLogLines, groupMode, errorType, path));
        }
        return logAnalyzeResults;
    }

    private static Function<String, String> getThreadDescriptionSupplier() {
        final Pattern rechDataflowPattern = Pattern.compile("RechnungMigrationDataflow|RechnungDataflow|reader XML-Reader-Migration");
        final Pattern mcdDataflowPattern = Pattern.compile("MCDDataflow|MCDMigrationDataflow");
        final Pattern fallbackPattern = Pattern.compile("\\[.+]");
        return logLine -> {
            Matcher mcdDataflowMatcher = mcdDataflowPattern.matcher(logLine);
            if (mcdDataflowMatcher.find()) {
                return mcdDataflowMatcher.group(0);
            }
            Matcher rechDataflowMatcher = rechDataflowPattern.matcher(logLine);
            if (rechDataflowMatcher.find()) {
                return rechDataflowMatcher.group(0);
            }
            Matcher fallbackMatcher = fallbackPattern.matcher(logLine);
            if (fallbackMatcher.find()) {
                return fallbackMatcher.group(0).replace("[", "").replace("]", "");
            }
            return "unknown thread";
        };
    }
}
