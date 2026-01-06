package ch.brugalibre.tooling.log.analyzer.publish;

import ch.brugalibre.tooling.log.analyzer.model.LogAnalyzeResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogAnalyzeResultFileExporter implements LogAnalyzeResultPublisher {
    @Override
    public void publish(LogAnalyzeResult logAnalyzeResult) throws IOException {
        String filename = logAnalyzeResult.groupMode().getDescription() + "-" 
                + logAnalyzeResult.errorType().getDescription() + "-analyzed-error.txt";
        Files.write(Path.of(logAnalyzeResult.inputPath(), filename), logAnalyzeResult.getContent(), StandardCharsets.UTF_8);
    }
}
