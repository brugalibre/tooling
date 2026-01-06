package ch.brugalibre.tooling.log.analyzer.group;

/**
 * The {@link GroupMode} enum defines the modes to group the different log lines.
 * It can be either DETAIL for detailed analysis or SUMMARY for a summarized overview.
 */
public enum GroupMode {
    DETAIL("Detail"),
    SUMMARY("Summary");

    private final String description;

    GroupMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
