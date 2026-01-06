package ch.brugalibre.tooling.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for handling command-line arguments.
 */
public class ArgumentHelper {

    public static final String ARG_DELIMITER = "--";

    private ArgumentHelper() {
        // private
    }

    /**
     * Retrieves the value associated with a specific argument from the given array of arguments.
     *
     * @param args     the array of command-line arguments
     * @param argument the argument whose value is to be retrieved
     * @return the value associated with the specified argument, or {@code null} if not found
     */
    public static String getValueForArg(String[] args, String argument) {
        List<String> values = getValueForArgument(args, argument, false);
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * Retrieves the value associated with a specific argument from the given array of arguments.
     * The extracted value is transformed using the provided transformer function.
     *
     * @param args     the array of command-line arguments
     * @param argument the argument whose value is to be retrieved
     * @return the value associated with the specified argument, or {@code null} if not found and not required
     */
    public static <R> R getValueForArg(String[] args, String argument, Function<String, R> transformer) {
        List<String> values = getValueForArgument(args, argument, false);
        return values.isEmpty() ? null : transformer.apply(values.get(0));
    }

    /**
     * Retrieves the value associated with a specific argument from the given array of arguments.
     * The extracted value is transformed using the provided transformer function.
     *
     * @param args     the array of command-line arguments
     * @param argument the argument whose value is to be retrieved
     * @return the value associated with the specified argument
     * @throws ArgumentNotPresentException is thrown when there is no value for the given argument
     */
    public static <R> R getValueForArgNonNull(String[] args, String argument, Function<String, R> transformer)
            throws ArgumentNotPresentException {
        List<String> values = getValueForArgument(args, argument, true);
        return transformer.apply(values.get(0));
    }

    /**
     * Retrieves the values associated with a specific argument from the given array of arguments.
     *
     * @param args     the array of command-line arguments
     * @param argument the argument whose value is to be retrieved
     * @return the value associated with the specified argument, or an empty list if not found
     */
    public static <R> List<R> getValuesForArg(String[] args, String argument, Function<String, R> transformer) {
        return getValueForArgument(args, argument, false).stream()
                .map(transformer)
                .collect(Collectors.toList());
    }

    private static List<String> getValueForArgument(String[] args, String argument, boolean required) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if ((ARG_DELIMITER + argument).equals(args[i]) && i + 1 < args.length) {
                do {
                    values.add(args[++i]);
                } while ((i + 1) < args.length && !args[i + 1].startsWith(ARG_DELIMITER));
            }
        }
        if (values.isEmpty() && required) {
            System.err.printf("Argument '%s' not found%n", argument);
            throw new ArgumentNotPresentException("Required argument " + argument + " not found");
        }
        return values;
    }
}
