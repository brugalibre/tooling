package ch.brugalibre.tooling.file.compare;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileContentComparer {

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: java FileContentComparer <path> <istFilesFile> <sollFilesFile> <operation>");
            return;
        }
        String path = args[0];
        String istFilesFile = args[1];
        String sollFilesFile = args[2];
        Operation operation = Operation.valueOf(args[3]);
        new FileContentComparer().compareFileContent(path, istFilesFile, sollFilesFile, operation);
    }

    private void compareFileContent(String path, String leftSideContentFile, String rightSideContentFile, Operation operation) throws IOException {
        List<String> leftSideContent = getFilesFromFile(path, leftSideContentFile);
        List<String> rightSideContent = getFilesFromFile(path, rightSideContentFile);

        if (operation == Operation.RIGHT_LEFT_DELTA) {
            Set<String> delta = new HashSet<>(rightSideContent);
            delta.removeAll(leftSideContent);
            System.out.println("delta: " + delta.size());
            Files.write(Path.of(path, "soll-ist-delta.txt"), List.of(String.join("|", delta)), StandardCharsets.UTF_8);
        } else if (operation == Operation.LEFT_INCLUDES_RIGHT) {
            List<String> leftSideContentContainsRightSide = leftSideContent.stream()
                    .filter(rightSideContent::contains)
                    .toList();
            Files.write(Path.of(path, "left-side-contains.txt"), List.of(String.join(",\r\n", leftSideContentContainsRightSide)), StandardCharsets.UTF_8);
        } else {
            System.err.println("Unknown operation: " + operation);
        }

//        String filesInDBFile = args[3];
//        String leftSideContentFile = args[4];
//        List<String> istFiles = getFilesFromFile(path, leftSideContentFile);
//        List<String> okFiles = getFilesFromFile(path, okFilesFile);
//        List<String> filesInDB = getFilesFromFile(path, filesInDBFile);
//        List<String> leftSideContent = getFilesFromFile(path, leftSideContentFile);
//
//        List<String> okFilesNotInDB = okFiles.stream()
//                .filter(not(filesInDB::contains))
//                .toList();
//
//        System.out.println("Ok files: " + okFiles.size());
//        if (!okFilesNotInDB.isEmpty() || okFiles.size() != filesInDB.size()) {
//            System.err.println("\tThere are inconsistencies in the OK files and DB files!");
//            System.err.println("\t" + "ok-files: " + okFiles.size());
//            System.err.println("\t" + "files-in-db: " + filesInDB.size());
//            System.err.println("\tOK-Files not in DB: (" + okFilesNotInDB.size() + ")" + String.join(",\n\t", okFilesNotInDB));
//            Files.write(Path.of(path, args[5]), okFilesNotInDB.stream().toList(), StandardCharsets.UTF_8);
//        } else {
//            System.out.println("\tOK files and DB files match.");
//        }
//        System.out.println("==============================================================");
//        
//        
//        System.out.println("\nFailed files: " + leftSideContent.size());
//        List<String> filesNotInDBButInFailed = leftSideContent.stream()
//                .filter(okFilesNotInDB::contains)
//                .toList();
//        System.out.println("\tamount ok files not in db but in failed files: " + filesNotInDBButInFailed.size());
//        System.out.println("\tok-files not in db but in failed:\n\t" + String.join(",\n\t", filesNotInDBButInFailed));
//        System.out.println("==============================================================");
//        
//        System.out.println("\nCompare ist files: " + istFiles.size());
//        List<String> istFilesNotInDB = istFiles.stream()
//                .filter(not(filesInDB::contains))
//                .toList();       
//        List<String> istFilesNotInDBNotInFailed = istFilesNotInDB.stream()
//                .filter(not(leftSideContent::contains))
//                .toList();
//        System.out.println("\tamount of ist-files which are not in db: " + istFilesNotInDB.size());
//        System.out.println("\tist-files which are not in db:\n\t" + String.join(",", istFilesNotInDB)); 
//        System.out.println("\tamount of ist-files (which are not in db) and also not in the failed files: " + istFilesNotInDBNotInFailed.size());
//        System.out.println("\tist-files (which are not in db) and also not in the failed files:\n\t" + String.join(",", istFilesNotInDBNotInFailed));
//        System.out.println("==============================================================");
//        
//        Files.write(Path.of(path, args[4].replace(".txt", "") + "okFilesNotInDB.txt"),
//                List.of(String.join(",\r\n", okFilesNotInDB)), StandardCharsets.UTF_8);
//
//        Files.write(Path.of(path, args[4].replace(".txt", "") + "istFilesNotInDB.txt"),
//                List.of(String.join(",\n", istFilesNotInDB)), StandardCharsets.UTF_8);
    }

    private static List<String> getFilesFromFile(String path, String failedFilesFile) throws IOException {
        return Files.readAllLines(Path.of(path, failedFilesFile), StandardCharsets.ISO_8859_1)
                .stream()
                .map(line -> line.replace(".fail", ""))
                .map(line -> line.replace(".ok", ""))
                .toList();
    }
}
