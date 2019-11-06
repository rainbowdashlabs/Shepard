package de.eldoria.shepard.localization.enums.commands.fun;

public enum OwoLocale {
    /**
     * Localization key for description.
     */
    DESCRIPTION("command.owo.description");

    /**
     * Get the escaped locale code for auto translation.
     */
    public final String tag;

    OwoLocale(String localeCode) {
        this.tag = "$" + localeCode + "$";
    }

    @Override
    public String toString() {
        return tag;
    }
}