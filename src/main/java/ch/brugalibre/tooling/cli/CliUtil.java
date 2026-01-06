package ch.brugalibre.tooling.cli;

import java.util.Scanner;

/**
 * Utility class for command-line interface (CLI) operations.
 */
public class CliUtil {

    private static final ThreadLocal<Scanner> SCANNER = ThreadLocal.withInitial(() -> new Scanner(System.in));

    private CliUtil() {
        // private
    }

    /**
     * Reads a value from the system input (console) after displaying a prompt.
     * This method blocks until the user provides input or cancels the operation (e.g., by pressing Ctrl+C).
     * @param prompt the prompt message to display to the user
     * @return the input value entered by the user
     */
    public static String readValueFromSystemIn(String prompt) {
        System.out.println(prompt);
        return SCANNER.get().nextLine();
    }
}
