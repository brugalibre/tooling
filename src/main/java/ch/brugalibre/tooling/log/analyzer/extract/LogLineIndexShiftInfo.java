package ch.brugalibre.tooling.log.analyzer.extract;

/**
 * Represents subsequent log line information along with the index shift which was necessary to find it.
 */
public record LogLineIndexShiftInfo(int indexShift, String logLine) {
}
