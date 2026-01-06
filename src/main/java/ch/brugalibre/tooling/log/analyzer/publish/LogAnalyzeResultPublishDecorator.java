package ch.brugalibre.tooling.log.analyzer.publish;

import ch.brugalibre.tooling.log.analyzer.model.LogAnalyzeResult;

import java.io.IOException;
import java.util.List;

public class LogAnalyzeResultPublishDecorator implements LogAnalyzeResultPublisher {
    private final List<LogAnalyzeResultPublisher> logAnalyzeResultPublishers;

    public LogAnalyzeResultPublishDecorator(List<LogAnalyzeResultPublisher> logAnalyzeResultPublishers) {
        this.logAnalyzeResultPublishers = logAnalyzeResultPublishers;
    }

    @Override
    public void publish(LogAnalyzeResult logAnalyzeResult) {
        logAnalyzeResultPublishers.forEach(logAnalyzeResultPublishers -> {
            try {
                logAnalyzeResultPublishers.publish(logAnalyzeResult);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
