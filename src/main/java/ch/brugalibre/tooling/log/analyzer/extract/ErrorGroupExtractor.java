package ch.brugalibre.tooling.log.analyzer.extract;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * Extracts error group and thread name from log lines based on the provided error type and a function to determine the dataflow origin.
 */
public class ErrorGroupExtractor {
    private final Function<String, String> dataflowOriginSupplier;

    /**
     * Constructor for ErrorGroupExtractor.
     * @param dataflowOriginSupplier a function that takes a log line and returns the dataflow origin (thread name)
     */
    public ErrorGroupExtractor(Function<String, String> dataflowOriginSupplier) {
        this.dataflowOriginSupplier = dataflowOriginSupplier;
    }

    /**
     * Extracts the error group and thread name from the given log line based on the specified error type.
     * @param errorType the type of error to extract information for
     * @param effectiveLogLine the log line that has been identified as containing an error
     * @param logLine the original log line, used to determine the thread name
     * @return a SimpleEntry containing the error group with thread name and the thread name separately; if no error group is found, both values will be null
     */
    public Entry<String, String> extractErrorGroupAndThreadName(ErrorType errorType, String effectiveLogLine, String logLine) {
        String logLineToExtratGroupFrom = effectiveLogLine != null ? effectiveLogLine : logLine;
        String group = extractErrorGrpFromLogLine(logLineToExtratGroupFrom, errorType);
        if (group != null) {
            String threadName = this.dataflowOriginSupplier.apply(logLine);
            return new AbstractMap.SimpleEntry<>(group + " (" + threadName + ")", threadName);
        }
        return new AbstractMap.SimpleEntry<>(null, null);
    }
    
    private static String extractErrorGrpFromLogLine(String effectiveLogLine, ErrorType errorType) {
        Matcher errorGrpMatcher = errorType.getErrorGrpPattern().matcher(effectiveLogLine);
        if (errorGrpMatcher.find()) {
            return extractFromMatcher(errorGrpMatcher, errorType);
        }
        return null;
    }

    private static String extractFromMatcher(Matcher errorGrpMatcher, ErrorType errorType) {
        if (ErrorType.RECH_USER_ASSIGNMENT == errorType) {
            return errorGrpMatcher.group(2).trim();
        }
        return errorGrpMatcher.group(1).endsWith(":") ? errorGrpMatcher.group(1) :
                errorGrpMatcher.group(1) + errorGrpMatcher.group(2).trim();
    }
}
