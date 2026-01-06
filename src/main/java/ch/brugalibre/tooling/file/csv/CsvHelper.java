package ch.brugalibre.tooling.file.csv;

import org.jooq.tools.csv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CsvHelper {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: CsvHelper <input-csv-file> <output-values-file> <separator> <column-to-extract-index>");
        }
        char separator = ',';
        if (args.length >= 3) {
            separator = args[2].charAt(0);
        }
        int colum = 0;
        if (args.length >= 4) {
            colum = Integer.parseInt(args[3]);
        }
        CsvHelper csvHelper = new CsvHelper();
        extractTransformAndExportValues(csvHelper, separator, args[0], args[1], colum);
        System.err.println("Column values extracted successfully.");
    }

    private static void extractTransformAndExportValues(CsvHelper csvHelper, char separator, String inputFilename,
                                                        String outputFilename, int colum) throws IOException {
        List<String> values = csvHelper.extractColumValuesFromCsv(colum, inputFilename, separator);
        List<String> transformedValues = getTransformedValues(values);
        Files.write(Path.of(outputFilename), transformedValues, StandardCharsets.UTF_8);
    }

    private static List<String> getTransformedValues(List<String> values) {
        values.sort(Comparator.naturalOrder());
        return List.of(String.join("',\r\n'", values));
    }

    private List<String> extractColumValuesFromCsv(int colum, String fileName, char separator) throws FileNotFoundException {
        List<String> values = new ArrayList<>();
        CSVReader csvReader = new CSVReader(new FileReader(fileName), separator);
        if (csvReader.hasNext()) {
            String[] nextValues = csvReader.next();
            while (nextValues != null) {
                values.add(nextValues[colum]);
                nextValues = csvReader.next();
            }
        }
        return values;
    }
}
