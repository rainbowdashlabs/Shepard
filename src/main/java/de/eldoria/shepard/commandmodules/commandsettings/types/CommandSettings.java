package de.eldoria.shepard.commandmodules.commandsettings.types;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.System.lineSeparator;

public class CommandSettings {
    private boolean adminOnly;
    private boolean nsfw;
    private boolean userCheckActive;
    private ListType userListType;
    private List<String> userList;
    private boolean guildCheckActive;
    private ListType guildListType;
    private List<String> guildList;
    private int userCooldown;
    private int guildCooldown;
    private Map<Long, Boolean> permissionOverride;

    /**
     * True if the command can be executed only by admin by default.
     * Can be overwritten by {@link #hasGuildPermissionOverride(Guild)}}
     *
     * @return true if this command is admin only by default.
     */
    public boolean isAdminOnly() {
        return adminOnly;
    }

    /**
     * Sets a context as admin only or not.
     *
     * @param adminOnly true if admin only should be on
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    /**
     * Get if the context is nsfw.
     *
     * @return true if context is nsfw
     */
    public boolean isNsfw() {
        return nsfw;
    }

    /**
     * Set context as nsfw.
     *
     * @param nsfw true if a context should be nsfw
     */
    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    /**
     * Get if the user check of the context is enabled.
     *
     * @return true if the user check is enabled
     */
    public boolean isUserCheckActive() {
        return userCheckActive;
    }

    /**
     * Set the user check.
     *
     * @param userCheckActive true if the user check should be enabled.
     */
    public void setUserCheckActive(boolean userCheckActive) {
        this.userCheckActive = userCheckActive;
    }

    /**
     * Gets user list type.
     *
     * @return List type enum.
     */
    public ListType getUserListType() {
        return userListType;
    }

    /**
     * Set user list type.
     *
     * @param userListType set list type as enum
     */
    public void setUserListType(ListType userListType) {
        this.userListType = userListType;
    }

    /**
     * Get user a list of users associated with this context.
     *
     * @return string list of user ids
     */
    public List<String> getUserList() {
        return userList;
    }

    /**
     * Set the user list.
     *
     * @param userList Sets a list of users.
     */
    public void setUserList(String[] userList) {
        this.userList = Arrays.asList(userList);
    }

    /**
     * Get if the guild check of the context is enabled.
     *
     * @return true if the guild check is enabled
     */
    public boolean isGuildCheckActive() {
        return guildCheckActive;
    }

    /**
     * Set the guild check.
     *
     * @param guildCheckActive true if the guild check should be enabled.
     */
    public void setGuildCheckActive(boolean guildCheckActive) {
        this.guildCheckActive = guildCheckActive;
    }

    /**
     * Gets guild list type.
     *
     * @return List type enum.
     */
    public ListType getGuildListType() {
        return guildListType;
    }

    /**
     * Set guild list type.
     *
     * @param guildListType set list type as enum
     */
    public void setGuildListType(ListType guildListType) {
        this.guildListType = guildListType;
    }

    /**
     * Get user a list of guilds associated with this context.
     *
     * @return string list of guild ids
     */
    public List<String> getGuildList() {
        return guildList;
    }

    /**
     * Set user list.
     *
     * @param guildList set guild list
     */
    public void setGuildList(String[] guildList) {
        this.guildList = Arrays.asList(guildList);
    }

    /**
     * Get the cooldown of the context for user.
     *
     * @return cooldown in seconds
     */
    public int getUserCooldown() {
        return userCooldown;
    }

    /**
     * Set the the cooldown of the context for user.
     *
     * @param userCooldown cooldown in seconds
     */
    public void setUserCooldown(int userCooldown) {
        this.userCooldown = userCooldown;
    }

    /**
     * Get the cooldown of the context for a guild.
     *
     * @return cooldown in seconds
     */
    public int getGuildCooldown() {
        return guildCooldown;
    }

    /**
     * Set the the cooldown of the context for a guild.
     *
     * @param guildCooldown cooldown in seconds
     */
    public void setGuildCooldown(int guildCooldown) {
        this.guildCooldown = guildCooldown;
    }

    /**
     * Check if a user and/or a guild cooldown is set.
     *
     * @return true if at least a guild or user cooldown is set.
     */
    public boolean hasCooldown() {
        return hasUserCooldown() || hasGuildCooldown();
    }

    /**
     * Check if a user cooldown is set.
     *
     * @return true if a cooldown is set
     */
    public boolean hasUserCooldown() {
        return userCooldown != 0;
    }

    /**
     * Check if a user cooldown is set.
     *
     * @return true if a cooldown is set
     */
    public boolean hasGuildCooldown() {
        return guildCooldown != 0;
    }

    /**
     * Set the permission override map.
     *
     * @param permissionOverride a map containing a boolean for each guild, which has a permission override set.
     */
    public void setPermissionOverride(Map<Long, Boolean> permissionOverride) {
        this.permissionOverride = permissionOverride;
    }

    /**
     * Check if the guild has a permission override.
     * If a permission override is set a admin command is no longer a admin command.
     * A non admin command is a admin command and needs a permission to be executed.
     *
     * @param guild guild to check if a override is active
     * @return true if a override is active.
     */
    public boolean hasGuildPermissionOverride(Guild guild) {
        Boolean override = permissionOverride.get(guild.getIdLong());
        return override == null ? false : override;
    }

    /**
     * Checks if a context needs a permission based on the permission override of the guild.
     *
     * @param guild guild for lookup
     * @return true of a permission is needed to execute this command
     */
    public boolean needsPermission(Guild guild) {
        return overrideActive(guild) != isAdminOnly();
    }

    /**
     * Checks if a permission override is active for this guild.
     *
     * @param guild guild for lookup
     * @return true if a override is active
     */
    public boolean overrideActive(Guild guild) {
        return hasGuildPermissionOverride(guild);
    }

    /**
     * Build the string for the command settings.
     * @param jda ja object
     * @return String which represents the information about the command.
     */
    public String buildString(JDA jda) {
        StringBuilder builder = new StringBuilder();
        builder.append("  admin_only: ").append(isAdminOnly()).append(lineSeparator())
                .append("  nsfw: ").append(isNsfw()).append(lineSeparator())
                .append("  user_cooldown: ").append(getUserCooldown()).append(lineSeparator())
                .append("  guild_cooldown: ").append(getGuildCooldown()).append(lineSeparator())
                .append("  user_check_active: ").append(isUserCheckActive()).append(lineSeparator());
        if (isUserCheckActive()) {
            builder.append("    List_Type: ").append(getUserListType()).append(lineSeparator())
                    .append("    Users_on_List: ").append(lineSeparator());
            List<String> names = new ArrayList<>();
            getUserList().forEach(u -> {
                User user = jda.getUserById(u);
                if (user != null) {
                    names.add("      " + user.getAsTag());
                }
            });
            builder.append(String.join(lineSeparator(), names))
                    .append(lineSeparator());
        }
        builder.append("  guild_check_active: ").append(isGuildCheckActive()).append(lineSeparator());
        if (isGuildCheckActive()) {
            builder.append("    List_Type: ")
                    .append(getGuildListType())
                    .append(lineSeparator());
            List<String> names = new ArrayList<>();
            getGuildList().forEach(g -> {
                Guild guild = jda.getGuildById(g);
                if (guild != null) {
                    Member member = guild.getOwner();
                    if (member != null) {
                        names.add("      " + guild.getName() + " by " + member.getUser().getAsTag());
                    }
                }
            });

            builder.append("    Guilds_on_List: ").append(lineSeparator())
                    .append(String.join(lineSeparator(), names)).append(lineSeparator());

        }
        return builder.toString();
    }

}
