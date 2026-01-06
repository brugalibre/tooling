package ch.brugalibre.tooling.log.analyzer.extract;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the archive path from log lines.
 */
public class ArchivePathExtractor {
    /**
     * Pattern to identify the log line indicating that the file reader could not access the next file
     */
    private static final Pattern UNABLE_TO_ACCESS_DATA_ENTRY_PATTERN = Pattern.compile("(.*)(Unable to access the next data entry.)$");

    /**
     * Pattern to identify the log line indicating a DataIOException caused by the file not being found
     */
    private static final Pattern DATA_IO_EXCEPTION = Pattern.compile("^(Caused by: ch.elca.server.dataflow.DataIOException:)(.*)");

    /**
     * Pattern to extract the sbv-archive path from the log line.
     */
    private static final Pattern SBV_PATH_PATTERN = Pattern.compile("(.*)(\\\\\\\\sbv.)(((\\w*)\\\\)+((.*).xml))(.*)");

    /**
     * Extracts the archive path for a file not found error from the log lines. In that case, the archive path is not
     * visible in the first log line, but in a subsequent log line (7 subsequent lines later from the next index to be precise...).
     *
     * @param errorLogLines the list of error log lines
     * @param logLine       the current log line being processed
     * @param nextIndex     the index of the next log line to be processed
     * @return a {@link LogLineIndexShiftInfo} containing the index shift and the extracted archive path. The extracted archive path can be null
     */
    public LogLineIndexShiftInfo extractArchivePathForFileNotFound(List<String> errorLogLines, String logLine,
                                                                   int nextIndex) {
        Matcher unableToAccessMatcher = UNABLE_TO_ACCESS_DATA_ENTRY_PATTERN.matcher(logLine);
        int lineCountUntilCause = 7;// well, this is a bit of a hack, but it works for the current stack trace
        if (unableToAccessMatcher.matches() && (nextIndex + lineCountUntilCause) < errorLogLines.size()) {
            return new LogLineIndexShiftInfo(lineCountUntilCause,
                    extractArchivePathFromLogLine(errorLogLines.get(nextIndex + lineCountUntilCause), DATA_IO_EXCEPTION));
        }
        return new LogLineIndexShiftInfo(0, null);
    }

    /**
     * Extracts the archive path from the given log line using the provided pattern.
     * Assumes that the archive path matching the pattern SBV_PATH_PATTERN and that it is in the second capturing group of the pattern
     *
     * @param logLine the log line to extract the archive path from
     * @return the extracted archive path, or <code>null</code> if no match is found
     */
    public String extractSbvArchivePathFromLogLine(String logLine) {
        return extractArchivePathFromLogLine(logLine, SBV_PATH_PATTERN);
    }

    private static String extractArchivePathFromLogLine(String logLine, Pattern archivePathPattern) {
        Matcher archivePathMatcher = archivePathPattern.matcher(logLine);
        if (archivePathMatcher.matches()) {
            if (archivePathMatcher.groupCount() >= 3) {
                return archivePathMatcher.group(2) + archivePathMatcher.group(3);
            }
            return archivePathMatcher.group(2);
        }
        return null;
    }
}
