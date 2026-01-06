package ch.brugalibre.tooling.log.analyzer.extract;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@link ErrorType} enum defines the types of errors that can be collected and analyzed.
 */
public enum ErrorType {

    /**
     * Error during the processing of invoices and mcd records
     * This contains errors during the scheduler execution as well as errors leading to a failed file
     */
    RECH_MCD_PROCESSING("RechMcd-Processing", List.of(ErrorPatternConst.SCHEDULER_EXECUTION,
            ErrorPatternConst.ABORT_DOCUMENT_IN_READER_PATTERN, ErrorPatternConst.DATAFLOW_ABORTED)
            , ErrorPatternConst.DEFAULT_ERROR_GROUP_PATTERN),

    /**
     * Only log lines origin from a user not assigned to a rech are collected & analyzed.
     */
    RECH_USER_ASSIGNMENT("User-Assignment", List.of(ErrorPatternConst.USER_NOT_ASSIGNED),
            ErrorPatternConst.USER_ASSIGNED_ERROR_GROUP_PATTERN),
    /**
     * All kind of errors / exception are collected & analyzed
     */
    ERRORS("Errors", List.of(ErrorPatternConst.ERROR_PATTERN_PLAIN, ErrorPatternConst.EXCEPTION_PATTERN),
            ErrorPatternConst.DEFAULT_ERROR_GROUP_PATTERN);

    private final List<Pattern> patterns;
    private final Pattern errorGrpPattern;
    private final String description;

    ErrorType(String description, List<Pattern> patterns, Pattern errorGrpPattern) {
        this.errorGrpPattern = errorGrpPattern;
        this.description = description;
        this.patterns = patterns;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public String getDescription() {
        return description;
    }

    public Pattern getErrorGrpPattern() {
        return errorGrpPattern;
    }

    public static class ErrorPatternConst {
        /**
         * Error indicating a non read file, resulting in a failed file
         */
        public static final Pattern ABORT_DOCUMENT_IN_READER_PATTERN = Pattern.compile("(.*)(Aborting document in reader XML-Reader(.*))$");

        /**
         * Log line indicating, that a user was could not be assigned to an invoice
         */
        public static final Pattern USER_NOT_ASSIGNED = Pattern.compile("(.*)(not found in LDAP, cannot assign user(.*))$");

        /**
         * Log line indicating an error during the scheduler execution
         */
        public static final Pattern SCHEDULER_EXECUTION = Pattern.compile("(.*)(SchedulerExecution)(.*)");

        /**
         * Log line indicating an abortion of the dataflow
         * (.*)(SchedulerExecution)(.*)(Dataflow processing)(.*)(aborted)(.*)
         * Example: SchedulerExecution -[id = '8544460'] Dataflow processing Rechnung_Nachfolgerechnung aborted
         */
        public static final Pattern DATAFLOW_ABORTED = Pattern.compile("(.*)(SchedulerExecution -)(\\[\\[])(id)(.*)(Dataflow processing)(.*)(aborted)(.*)");

        /**
         * Common error pattern for plain text errors in logs, such as "SumexError: ..."
         */
        public static final Pattern ERROR_PATTERN_PLAIN = Pattern.compile("(.*)(Error:)(.*)");
        public static final Pattern EXCEPTION_PATTERN = Pattern.compile("(.*)(Exception:)(.*)");
        public static final Pattern EXCEPTION_CAUSED_BY_PATTERN = Pattern.compile("(.*)(Caused by:)(.*)");

        /**
         * Pattern to extract the error group from the log line.
         */
        private static final Pattern DEFAULT_ERROR_GROUP_PATTERN = Pattern.compile("(.*)(\\w+:)(.*)");

        /**
         * Pattern to extract the error group from the log line.
         */
        private static final Pattern USER_ASSIGNED_ERROR_GROUP_PATTERN = Pattern.compile("(.*)(not found in LDAP, cannot assign user)(.*)");
    }
}
