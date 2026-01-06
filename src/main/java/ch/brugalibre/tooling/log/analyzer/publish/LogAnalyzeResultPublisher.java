package ch.brugalibre.tooling.log.analyzer.publish;

import ch.brugalibre.tooling.log.analyzer.model.LogAnalyzeResult;

import java.io.IOException;

/**
 * The {@link LogAnalyzeResultPublisher} interface defines a contract for publishing the results of log analysis.
 * Implementations of this interface can provide different ways to publish the results, such as printing to console,
 * writing to a file, or sending over a network.
 */
public interface LogAnalyzeResultPublisher {

    /**
     * Publishes the given log analysis result.
     *
     * @param logAnalyzeResult the result of the log analysis to be published
     */
    void publish(LogAnalyzeResult logAnalyzeResult) throws IOException;
}
