package ch.brugalibre.tooling.log.analyzer.model;

import ch.brugalibre.tooling.log.analyzer.extract.ErrorType;
import ch.brugalibre.tooling.log.analyzer.group.GroupMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link LogAnalyzeResult} represents the result of analyzing multiple log files.
 *
 * @param logLineDetails the list of analyzed log file details, each containing log line details
 * @param groupMode      groupMode the mode of analysis to determine how the results are represented
 * @param errorType      the type of error that was analyzed
 * @param inputPath      the directory where the log files were located
 */
public record LogAnalyzeResult(List<LogLineDetail> logLineDetails, GroupMode groupMode, ErrorType errorType,
                               String inputPath) {
    /**
     * Returns a list of string representations of the log file analysis results based on the specified analyze mode.
     *
     * @return a list of string representations of the log file analysis results
     */
    public List<String> getContent() {
        List<String> content = logLineDetails().stream()
                .map(logDetail -> logDetail.getRepresentation(groupMode))
                .collect(Collectors.toCollection(ArrayList::new));
        content.add(0, getHeaderRepresentation());
        return content;
    }

    public String getRepresentation() {
        return getHeaderRepresentation() 
                + logLineDetails.stream()
                .map(logLineDetail -> logLineDetail.getRepresentation(groupMode))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String getHeaderRepresentation() {
        return "Logs analyzed (" + getAnalyzeDetails() + ")" + System.lineSeparator() +
                "Total error count: " + getTotalCount() + System.lineSeparator() +
                getCountPerThreadName().entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + entry.getValue())
                        .collect(Collectors.joining(System.lineSeparator()));
    }

    private int getTotalCount() {
        return logLineDetails.stream()
                .mapToInt(LogLineDetail::getCount)
                .sum();
    }

    private Map<String, Integer> getCountPerThreadName() {
        return logLineDetails.stream()
                .collect(Collectors.toMap(
                        LogLineDetail::getThreadName,
                        LogLineDetail::getCount,
                        Integer::sum));
    }
    
    private String getAnalyzeDetails() {
        return "group-mode: " + groupMode.getDescription() + ", error-type: "
                + errorType.getDescription();
    }
}
