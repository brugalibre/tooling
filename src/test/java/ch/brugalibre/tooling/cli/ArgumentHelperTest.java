package ch.brugalibre.tooling.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

class ArgumentHelperTest {

    @Test
    void getValueForArgNonNullHappyCase() {
        // Given
        String argumentName = "booleanType";
        String[] args = new String[]{"--" + argumentName, "true"};

        // When
        Boolean value = ArgumentHelper.getValueForArg(args, argumentName, Boolean::valueOf);

        // Then
        Assertions.assertNotNull(value);
        Assertions.assertTrue(value);
    }

    @Test
    void getValueForArgNonNullIsNull() {
        // Given
        String argumentName = "booleanType";
        String[] args = new String[]{"--" + argumentName, "true"};

        // When
        Boolean value = ArgumentHelper.getValueForArg(args, "wrongArgument", Boolean::valueOf);

        // Then
        Assertions.assertNull(value);
    }

    @Test
    void getValueForArgIsNull() {
        // Given
        String argumentName = "booleanType";
        String[] args = new String[]{"--" + argumentName, "true"};

        // When
        Executable exec = () -> ArgumentHelper.getValueForArgNonNull(args, "wrongArgument", Boolean::valueOf);

        // Then
        Assertions.assertThrows(ArgumentNotPresentException.class, exec);
    }

    @Test
    void getValuesForArg() {
        // Given
        String argumentName = "booleanType";
        String[] args = new String[]{"--" + argumentName, "true", "false", "true"};

        // When
        List<Boolean> values = ArgumentHelper.getValuesForArg(args, argumentName, Boolean::valueOf);

        // Then
        Assertions.assertNotNull(values);
        Assertions.assertEquals(List.of(true, false, true), values);
    }
}