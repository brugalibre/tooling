package ch.brugalibre.tooling.file.compare;

public enum Operation {
    /**
     * Compare the files in the "ist" list with the "soll" list and return the files that are in "ist" but not in "soll".
     */
    RIGHT_LEFT_DELTA,
    LEFT_INCLUDES_RIGHT,
}
