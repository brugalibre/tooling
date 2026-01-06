package ch.brugalibre.tooling.log.analyzer.group;

import ch.brugalibre.tooling.log.analyzer.model.LogAnalyzeResult;
import ch.brugalibre.tooling.log.analyzer.model.LogLineDetail;
import ch.brugalibre.tooling.log.analyzer.model.MetaInformation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link LogLineGrouper} class is responsible for grouping and sorting log line details by their error group.
 * It processes a {@link LogAnalyzeResult} and returns a new result with grouped log line details.
 */
public class LogLineGrouper {

    /**
     * Groups the {@link LogLineDetail} by their error group.
     *
     * @param logLineDetailsIn the result containing log file analyze results
     * @param groupMode        the mode of analysis (e.g., DETAIL or SUMMARY)
     * @return a list with grouped and sorted {@link LogLineDetail}s
     */
    public List<LogLineDetail> groupLogLineDetails(List<LogLineDetail> logLineDetailsIn, GroupMode groupMode) {
        List<LogLineDetail> logLineDetails = removeDuplicates(logLineDetailsIn);
        return logLineDetails.stream()
                .collect(Collectors.groupingBy(LogLineDetail::getErrorGroup))
                .entrySet().stream()
                .map(errorGroup -> {
                    // Only threadname is unique across all log lines in the group. the same errors may have occurred in different logfiles
                    String threadName = errorGroup.getValue().isEmpty() ? null : errorGroup.getValue().get(0).getThreadName();
                    MetaInformation metaInformation = new MetaInformation(null, null, errorGroup.getKey(), threadName);
                    LogLineDetail parentGroupLogLine = new LogLineDetail(null, metaInformation);
                    Comparator<LogLineDetail> logLineDetailComparator = Comparator.comparing(LogLineDetail::getLogFileName)
                            .thenComparing(Comparator.comparingInt(LogLineDetail::getCount).reversed());
                    parentGroupLogLine.addChildren(errorGroup.getValue(), groupMode, logLineDetailComparator);
                    return parentGroupLogLine;
                })
                .sorted(Comparator.comparing(LogLineDetail::getCount).reversed())
                .toList();
    }

    private static List<LogLineDetail> removeDuplicates(List<LogLineDetail> logLineDetails) {
        List<LogLineDetail> uniqueLogLineDetails = new ArrayList<>();
        for (LogLineDetail logLineDetail : logLineDetails) {
            boolean isDuplicate = uniqueLogLineDetails.stream()
                    .anyMatch(uniqueLLD -> uniqueLLD.isDuplicate(logLineDetail));
//            if (!isDuplicate) {
                uniqueLogLineDetails.add(logLineDetail);
//            }
        }
        return uniqueLogLineDetails;
    }
}
