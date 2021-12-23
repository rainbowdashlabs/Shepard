package de.eldoria.shepard.localization.enums.commands.util;

public enum HireMeLocale {
    /**
     * Localization key for description.
     */
    DESCRIPTION("command.hireMe.description"),
    /**
     * Localization key for message hire me.
     */
    M_HIRE_ME("command.hireMe.message.hireMe"),
    /**
     * Localization key for message i want you.
     */
    M_I_WANT_YOU("command.hireMe.message.iWantyou"),
    /**
     * Localization key for message take me.
     */
    M_TAKE_ME("command.hireMe.message.takeMe");

    /**
     * Get the escaped locale code for auto translation.
     */
    public final String tag;

    /**
     * Create a new locale object.
     *
     * @param localeCode locale code
     */
    HireMeLocale(String localeCode) {
        this.tag = "$" + localeCode + "$";
    }

    @Override
    public String toString() {
        return tag;
    }
}
