package ch.brugalibre.tooling.log.analyzer.model;

import ch.brugalibre.tooling.log.analyzer.group.GroupMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A {@link LogLineDetail} represents a detailed log line with its associated metadata, like the XML file name and error group.
 */
public class LogLineDetail {
    static final String LOG_LINE_SEPARATOR = System.lineSeparator() + "\t";
    private final MetaInformation metaInformation;
    private final List<LogLineDetail> children;
    private final String logLine;

    private LogLineDetail parent;
    private int count;

    /**
     * Creates a new instance of LogLineDetail.
     *
     * @param logLine         the log line text
     * @param metaInformation contains metadata about the log line, such as log file name, XML file name, error group, and thread name
     */
    public LogLineDetail(String logLine, MetaInformation metaInformation) {
        this(null, logLine, metaInformation);
    }

    /**
     * Creates a new instance of LogLineDetail.
     *
     * @param parent          the parent LogLineDetail, can be null for root level
     * @param logLine         the log line text
     * @param metaInformation contains metadata about the log line, such as log file name, XML file name, error group, and thread name
     */
    public LogLineDetail(LogLineDetail parent, String logLine, MetaInformation metaInformation) {
        this.children = new ArrayList<>();
        this.parent = parent;
        this.metaInformation = metaInformation;
        this.count = 1;
        this.logLine = logLine;
    }

    /**
     * Adds new children to this LogLineDetail instance. If the group mode is SUMMARY, it checks if the log line already exists
     * Each child is added as a copy to avoid modifying the original list.
     *
     * @param newChildren the list of new LogLineDetail instances to be added as children
     * @param groupMode   the mode of grouping (e.g., SUMMARY or DETAIL)
     * @param comparator  the comparator used to sort the children after adding them
     */
    public void addChildren(List<LogLineDetail> newChildren, GroupMode groupMode, Comparator<LogLineDetail> comparator) {
        if (groupMode == GroupMode.SUMMARY) {
            for (LogLineDetail logLineDetail : newChildren) {
                if (containsLogFromSameLogFile(logLineDetail)) {
                    incrementLogOnChild(logLineDetail.getErrorGroup(), logLineDetail.getLogLine());
                } else {
                    this.children.add(logLineDetail.copy());
                }
            }
        } else {
            this.children.addAll(newChildren.stream()
                    .map(LogLineDetail::copy)
                    .toList());
        }
        this.children.sort(comparator);
        this.children.forEach(child -> child.parent = this);
    }

    /**
     * Creates a copy of this LogLineDetail instance. Children and count are reset to their initial state.
     *
     * @return a new LogLineDetail instance with the same log line and meta information, but without children and with count set to 1
     */
    public LogLineDetail copy() {
        return new LogLineDetail(this.parent, this.logLine, this.metaInformation);
    }

    public void incrementCount() {
        this.count++;
    }

    public String getLogLine() {
        return logLine;
    }

    public String getLogFileName() {
        return metaInformation.logFileName();
    }

    public String getErrorGroup() {
        return metaInformation.errorGroup();
    }

    public String getThreadName() {
        return metaInformation.threadName();
    }

    public boolean isDuplicate(LogLineDetail otherLogLine) {
        MetaInformation metaInformation = getMetaInformation();
        return Objects.equals(logLine, otherLogLine.logLine)
                && metaInformation.xmlFileName() != null && Objects.equals(metaInformation.xmlFileName(), otherLogLine.getMetaInformation().xmlFileName())
                && Objects.equals(metaInformation.errorGroup(), otherLogLine.getMetaInformation().errorGroup());
    }

    public String getRepresentation(GroupMode groupMode) {
        return (parent == null ? System.lineSeparator() + metaInformation.errorGroup() : LOG_LINE_SEPARATOR)
                + (logLine == null ? "" : logLine)
                + getMetaInformationRepresentation(groupMode)
                + childsToString(children, groupMode);
    }

    public int getCount() {
        return children.isEmpty() ? count : children.stream()
                .map(LogLineDetail::getCount)
                .reduce(0, Integer::sum);
    }

    private boolean containsLogFromSameLogFile(LogLineDetail lastLogLine) {
        return children.stream()
                .anyMatch(child -> Objects.equals(child.getLogLine(), lastLogLine.logLine)
                        && Objects.equals(child.getLogFileName(), lastLogLine.getLogFileName()));
    }

    private void incrementLogOnChild(String errorGroup, String logLine) {
        children.stream()
                .filter(child -> Objects.equals(child.getLogLine(), logLine)
                        && Objects.equals(child.getErrorGroup(), errorGroup))
                .findFirst()
                .ifPresent(LogLineDetail::incrementCount);
    }

    private String childsToString(List<LogLineDetail> childs, GroupMode groupMode) {
        if (childs.isEmpty()) {
            return "";
        }
        List<String> list = childs.stream()
                .map(childLogLineDetail -> childLogLineDetail.getRepresentation(groupMode))
                .map(line -> line.replaceFirst(System.lineSeparator(), ""))
                .toList();
        return LOG_LINE_SEPARATOR + String.join(LOG_LINE_SEPARATOR, list);
    }

    private String getMetaInformationRepresentation(GroupMode groupMode) {
        List<String> metaInfo = new ArrayList<>();
        if (this.parent != null && groupMode != GroupMode.SUMMARY) {
            metaInfo.add("xml-file: " + metaInformation.xmlFileName());
        } else {
            metaInfo.add("count: " + getCount());
        }
        if (metaInformation.logFileName() != null) {
            metaInfo.add("logfile: " + metaInformation.logFileName());
        }
        return "\t(" + String.join(", ", metaInfo) + ")";
    }

    public MetaInformation getMetaInformation() {
        return metaInformation;
    }
}
