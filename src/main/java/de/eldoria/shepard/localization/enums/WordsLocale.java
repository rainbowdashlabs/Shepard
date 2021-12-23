package de.eldoria.shepard.localization.enums;

public enum WordsLocale {
    /**
     * Localization key for word "Name".
     */
    NAME("words.name"),
    /**
     * Localization key for word "ID".
     */
    ID("words.id"),
    /**
     * Localization key for word "Address".
     */
    ADDRESS("words.address"),
    /**
     * Localization key for word "Minecraft".
     */
    MINECRAFT("words.minecraft"),
    /**
     * Localization key for word "Context Name".
     */
    CONTEXT_NAME("words.contextName"),
    /**
     * Localization key for word "Keyword".
     */
    KEYWORD("words.keyword"),
    /**
     * Localization key for word "Keyword".
     */
    MESSAGE("words.message"),
    /**
     * Localization key for word "Keyword".
     */
    TIME("words.time"),
    /**
     * Localization key for word "Category".
     */
    CATEGORY("words.category"),
    /**
     * Localization key for word "Point".
     */
    POINT("words.point"),
    /**
     * Localization key for word "Points".
     */
    POINTS("words.points"),
    RANK("words.rank"),
    USER("words.user"),
    INVALID("words.invalid"),
    ROLE("words.role"),
    STATE("words.state"),
    ENABLED("words.enabled"),
    DISABLED("words.disabled");

    /**
     * Get the escaped locale code for auto translation.
     */
    public final String tag;

    /**
     * Create a new locale object.
     *
     * @param localeCode locale code
     */
    WordsLocale(String localeCode) {
        this.tag = "$" + localeCode + "$";
    }

    @Override
    public String toString() {
        return tag;
    }
}
