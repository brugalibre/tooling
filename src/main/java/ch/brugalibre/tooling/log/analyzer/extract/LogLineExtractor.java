package ch.brugalibre.tooling.log.analyzer.extract;

import ch.brugalibre.tooling.log.analyzer.model.LogLineDetail;
import ch.brugalibre.tooling.log.analyzer.model.MetaInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for extracting log lines from log files in a specified directory, based on certain 
 * error types and exclusion patterns.
 */
public class LogLineExtractor {

    /**
     * Pattern to match log lines indicating a failed file due to an error in the XML reader or due to an exception/error.
     */
    private static final Pattern ERRORS_FOUND_PATTERN = Pattern.compile("(.*)(sumex_ii.common.dataflow.XMLSumexError: \\d+ errors found:)$");

    private final ErrorGroupExtractor errorGroupExtractor;
    private final ArchivePathExtractor archivePathExtractor;

    /**
     * Constructor for {@link LogLineExtractor}.
     *
     * @param dataflowOriginSupplier a function that takes a log line and returns the dataflow origin (thread name)
     */
    public LogLineExtractor(Function<String, String> dataflowOriginSupplier) {
        this.errorGroupExtractor = new ErrorGroupExtractor(dataflowOriginSupplier);
        this.archivePathExtractor = new ArchivePathExtractor();
    }

    /**
     * Extracts log files from a specified directory for errors of the given type, excluding lines that match any of the provided patterns.
     *
     * @param inputDir        the directory containing log files to analyze
     * @param errorType       the type of error to analyze (e.g., FAILED_FILES or PLAIN_ERRORS)
     * @param excludedPattern a list of patterns to exclude certain log lines from the analysis; if a log line matches any of these patterns, it will be skipped
     * @return a list containing extracted {@link LogLineDetail}s from given input directory
     * @throws IOException if an I/O error occurs while reading the log files
     */
    public List<LogLineDetail> extractLogLinesFromDirectory(String inputDir, ErrorType errorType, List<Pattern> excludedPattern)
            throws IOException {
        System.out.println("Analyzing logs from: " + inputDir);
        return Files.list(Paths.get(inputDir))
                .filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".log"))
                .map(filePath -> extractLogLinesFromFile(errorType, excludedPattern, filePath))
                .flatMap(List::stream)
                .toList();
    }

    private List<LogLineDetail> extractLogLinesFromFile(ErrorType errorType, List<Pattern> excludedPattern, Path filePath) {
        try {
            List<String> errorLogLines = Files.readAllLines(filePath, StandardCharsets.ISO_8859_1);
            return filterAndCollectExtractedLogLines(errorType, excludedPattern, errorLogLines, filePath.getFileName().toString());
        } catch (Exception e) {
            System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
            return List.of();
        }
    }

    private List<LogLineDetail> filterAndCollectExtractedLogLines(ErrorType errorType, List<Pattern> excludedPattern,
                                                                  List<String> errorLogLines, String logFileName) {
        List<LogLineDetail> logLineDetails = new ArrayList<>();
        for (int index = 0; index < errorLogLines.size(); index++) {
            String logLine = errorLogLines.get(index);
            String effectiveLogLine;
            if (logLine.startsWith("2026-01-05T22:18:05,554 ERROR [6191")){
                System.err.println();
            }
            if (isLogLineMatch(errorType.getPatterns(), logLine)) {
                String xmlFileName = archivePathExtractor.extractSbvArchivePathFromLogLine(logLine);
                LogLineIndexShiftInfo subsequentLogLineInfo;
                if (hasSubsequentErrorLine(errorType)) {
                    subsequentLogLineInfo = getSubsequentLogLine(logLine, index, index + 1, errorLogLines);
                    effectiveLogLine = subsequentLogLineInfo.logLine();// effectiveLogLine is never null for 'FAILED_FILES' but may be null for any other pattern

                    // When the effectiveLogLine looks like 'Unable to access the next data entry' then the archive path is not in the first line, but in a subsequent line.
                    if (xmlFileName == null && effectiveLogLine != null) {
                        LogLineIndexShiftInfo xmlFileNameIndexShiftInfo = archivePathExtractor.
                                extractArchivePathForFileNotFound(errorLogLines, effectiveLogLine, index + 1);
                        xmlFileName = xmlFileNameIndexShiftInfo.logLine();
                        index = index + xmlFileNameIndexShiftInfo.indexShift();
                    }
                } else {
                    subsequentLogLineInfo = getSubsequentLogLineForErrorsFoundLog(logLine, index, index, errorLogLines);
                    effectiveLogLine = subsequentLogLineInfo.logLine() == null ? logLine : subsequentLogLineInfo.logLine();
                }
                index = index + subsequentLogLineInfo.indexShift();
                Entry<String, String> errorGroupAndThreadName = this.errorGroupExtractor.extractErrorGroupAndThreadName(errorType, effectiveLogLine, logLine);
                if (!isExcluded(effectiveLogLine, logLine, excludedPattern)) {
                    MetaInformation metaInformation = new MetaInformation(logFileName, xmlFileName,
                            errorGroupAndThreadName.getKey(), errorGroupAndThreadName.getValue());
                    logLineDetails.add(new LogLineDetail(effectiveLogLine, metaInformation));
                }
            }
        }
        return logLineDetails;
    }

    /**
     * The error type {@link ErrorType#RECH_MCD_PROCESSING} is always followed by the actual error that
     * occurred during the processing of the rech/mcd. The first line is just meta information which indicates an exception.
     * The actual exception is therefore in a subsequent log line
     */
    private static boolean hasSubsequentErrorLine(ErrorType errorType) {
        return errorType == ErrorType.RECH_MCD_PROCESSING;
    }

    private static boolean isLogLineMatch(List<Pattern> allPattern, String logLine) {
        if (matchesCausedByPattern(logLine)){
            return false;
        }
        return allPattern.stream()
                .map(pattern -> pattern.matcher(logLine))
                .anyMatch(Matcher::matches);
    }

    private static boolean isExcluded(String effectiveLogLine, String logLine, List<Pattern> excludedPattern) {
        if (matchesCausedByPattern(logLine)){
            return true;
        }
        for (Pattern patternToExclude : excludedPattern) {
            if (effectiveLogLine == null 
                    || patternToExclude.matcher(effectiveLogLine).matches()
                    || patternToExclude.matcher(logLine).matches()) {
                System.out.println("Excluding line: " + effectiveLogLine);
                return true; // Skip this line if it matches any excluded pattern
            }
        }
        return false; // Include the line if no patterns match
    }

    private static boolean matchesCausedByPattern(String logLine) {
        return ErrorType.ErrorPatternConst.EXCEPTION_CAUSED_BY_PATTERN.matcher(logLine).matches();
    }

    private static LogLineIndexShiftInfo getSubsequentLogLine(String logLine, int currentIndex, int nextIndex,
                                                              List<String> errorLogLines) {
        if (nextIndex < errorLogLines.size()) {
            String nextLine = errorLogLines.get(nextIndex);
            Matcher exceptionMatcher = ERRORS_FOUND_PATTERN.matcher(nextLine);
            if (exceptionMatcher.matches()) {
                return getSubsequentLogLineForErrorsFoundLog(nextLine, currentIndex, nextIndex, errorLogLines);
            }
            return new LogLineIndexShiftInfo(1, nextLine);// We got that logline with 'nextIndex' which is by nature + 1 to the current index
        } else {
            return getSubsequentLogLineForErrorsFoundLog(logLine, currentIndex, currentIndex, errorLogLines);
        }
    }

    private static LogLineIndexShiftInfo getSubsequentLogLineForErrorsFoundLog(String logLine, int prevIndex, int index,
                                                                               List<String> errorLogLines) {
        Matcher errorFoundPatternMatches = ERRORS_FOUND_PATTERN.matcher(logLine);
        if (errorFoundPatternMatches.matches()
                && index + 1 < errorLogLines.size()) {
            return new LogLineIndexShiftInfo((index + 1 - prevIndex), errorLogLines.get(index + 1));
        }
        return new LogLineIndexShiftInfo(0, null);
    }
}
