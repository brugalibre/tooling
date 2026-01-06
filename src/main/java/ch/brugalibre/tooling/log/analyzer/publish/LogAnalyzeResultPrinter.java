package ch.brugalibre.tooling.log.analyzer.publish;

import ch.brugalibre.tooling.log.analyzer.model.LogAnalyzeResult;

public class LogAnalyzeResultPrinter implements LogAnalyzeResultPublisher {
    @Override
    public void publish(LogAnalyzeResult logAnalyzeResult) {
        System.out.println("Done analyzing print results:");
        System.out.println(logAnalyzeResult.getRepresentation());
        System.out.println(System.lineSeparator() + "Done analyzing, see result file for details");
    }
}
