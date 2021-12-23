package de.eldoria.shepard.localization.enums.commands.admin;

public enum TicketSettingsLocale {
    /**
     * Localization key for description.
     */
    DESCRIPTION("command.ticketSettings.description"),
    /**
     * Localization key for subcommand createType.
     */
    C_CREATE_TYPE("command.ticketSettings.subcommand.createType"),
    /**
     * Localization key for subcommand removeType.
     */
    C_REMOVE_TYPE("command.ticketSettings.subcommand.removeType"),
    /**
     * Localization key for subcommand setOwnerRoles.
     */
    C_SET_OWNER_ROLES("command.ticketSettings.subcommand.setOwnerRoles"),
    /**
     * Localization key for subcommand setSupportRoles.
     */
    C_SET_SUPPORT_ROLES("command.ticketSettings.subcommand.setSupportRoles"),
    /**
     * Localization key for subcommand setCategory.
     */
    C_SET_CATEGORY("command.ticketSettings.subcommand.setCategory"),
    /**
     * Localization key for subcommand setCreationMessage.
     */
    C_SET_CREATION_MESSAGE("command.ticketSettings.subcommand.setCreationMessage"),
    /**
     * Localization key for message create type.
     */
    M_CREATE_TYPE("command.ticketSettings.message.createdType"),
    /**
     * Localization key for message remove type.
     */
    M_REMOVE_TYPE("command.ticketSettings.message.removedType"),
    /**
     * Localization key for message set owner roles.
     */
    M_SET_OWNER_ROLES("command.ticketSettings.message.setOwnerRoles"),
    /**
     * Localization key for message set support roles.
     */
    M_SET_SUPPORT_ROLES("command.ticketSettings.message.setSupportRoles"),
    /**
     * Localization key for message set category.
     */
    M_SET_CATEGORY("command.ticketSettings.message.setCategory"),
    /**
     * Localization key for message set creation message.
     */
    M_SET_CREATION_MESSAGE("command.ticketSettings.message.setCreationMessage");

    /**
     * Get the escaped locale code for auto translation.
     */
    public final String tag;

    /**
     * Create a new locale object.
     *
     * @param localeCode locale code
     */
    TicketSettingsLocale(String localeCode) {
        this.tag = "$" + localeCode + "$";
    }

    @Override
    public String toString() {
        return tag;
    }
}
