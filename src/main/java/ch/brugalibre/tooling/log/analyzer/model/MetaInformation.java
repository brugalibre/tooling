package ch.brugalibre.tooling.log.analyzer.model;

/**
 * MetaInformation holds metadata about a log file, including the log file name, XML file name, error group, and thread name.
 * @param logFileName the name of the log file where this log line was found
 * @param xmlFileName the name of the XML file associated with this log line, can be null for summary mode or root level
 * @param errorGroup  the error group this log line belongs to, used for grouping similar errors 
 * @param threadName the name of the thread that processed this log line, can be null if not applicable
 */
public record MetaInformation(String logFileName, String xmlFileName, String errorGroup, String threadName) {
}
