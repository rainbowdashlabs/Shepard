package de.eldoria.shepard.localization.enums.commands.fun;

public enum RandomJokeLocale {
    /**
     * Localization key for description.
     */
    DESCRIPTION("command.randomJoke.description"),
    /**
     * Localization key for message joke.
     */
    M_JOKE("command.randomJoke.message.joke");

    /**
     * Get the escaped locale code for auto translation.
     */
    public final String tag;

    /**
     * Create a new locale object.
     *
     * @param localeCode locale code
     */
    RandomJokeLocale(String localeCode) {
        this.tag = "$" + localeCode + "$";
    }

    @Override
    public String toString() {
        return tag;
    }
}
