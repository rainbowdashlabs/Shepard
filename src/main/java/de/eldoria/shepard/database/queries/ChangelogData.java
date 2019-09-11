package de.eldoria.shepard.database.queries;

import de.eldoria.shepard.database.DatabaseConnector;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.eldoria.shepard.database.DbUtil.handleException;
import static de.eldoria.shepard.database.DbUtil.handleExceptionAndIgnore;

public final class ChangelogData {

    private ChangelogData() {
    }

    /**
     * Adds a role to changelog observation.
     *
     * @param guild  guild on which the role should be added
     * @param role id of the role id of the role
     * @param event  event from command sending for error handling. Can be null.
     */
    public static boolean addRole(Guild guild, Role role, MessageReceivedEvent event) {
        try (PreparedStatement statement = DatabaseConnector.getConn()
                .prepareStatement("SELECT shepard_func.add_changelog_role(?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, role.getId());
            statement.execute();
        } catch (SQLException e) {
            handleExceptionAndIgnore(e, event);
            return false;
        }
        return true;
    }

    /**
     * Remove a role from changelog observation.
     *
     * @param guild  Guild object for lookup
     * @param role id of the role
     * @param event  event from command sending for error handling. Can be null.
     */
    public static boolean removeRole(Guild guild, Role role, MessageReceivedEvent event)  {
        try (PreparedStatement statement = DatabaseConnector.getConn()
                .prepareStatement("SELECT shepard_func.remove_changelog_role(?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, role.getId());
            statement.execute();
        } catch (SQLException e) {
            handleExceptionAndIgnore(e, event);
            return false;
        }
        return true;
    }

    /**
     * Sets the changelog channel.
     *
     * @param guild     Guild object for lookup
     * @param channel Id of the channel
     * @param event     event from command sending for error handling. Can be null.
     */
    public static void setChannel(Guild guild, TextChannel channel, MessageReceivedEvent event) throws SQLException {
        try (PreparedStatement statement = DatabaseConnector.getConn()
                .prepareStatement("SELECT shepard_func.set_changelog_channel(?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, channel.getId());
            statement.execute();
        } catch (SQLException e) {
            handleException(e, event);
        }
    }

    /**
     * Removes the changelog channel.
     *
     * @param guild Guild object for lookup
     * @param event event from command sending for error handling. Can be null.
     */
    public static boolean removeChannel(Guild guild, MessageReceivedEvent event) {
        try (PreparedStatement statement = DatabaseConnector.getConn()
                .prepareStatement("SELECT shepard_func.remove_changelog_channel(?)")) {
            statement.setString(1, guild.getId());
            statement.execute();
        } catch (SQLException e) {
            handleExceptionAndIgnore(e, event);
            return false;
        }
        return true;
    }

    /**
     * Get a list of all observed roles.
     *
     * @param guild Guild object for lookup
     * @param event event from command sending for error handling. Can be null.
     * @return list of role ids
     */
    public static List<String> getRoles(Guild guild, MessageReceivedEvent event) {
        try (PreparedStatement statement = DatabaseConnector.getConn()
                .prepareStatement("SELECT shepard_func.get_changelog_roles(?)")) {
            statement.setString(1, guild.getId());
            ResultSet result = statement.executeQuery();
            if (result.next() && result.getArray(1) != null) {
                return Arrays.asList((String[]) result.getArray(1).getArray());
            }
        } catch (SQLException e) {
            handleExceptionAndIgnore(e, event);
        }
        return Collections.emptyList();
    }

    /**
     * Get the changelog channel of the guild.
     *
     * @param guild Guild object for lookup
     * @param event event from command sending for error handling. Can be null.
     * @return channel id as string
     */
    public static String getChannel(Guild guild, MessageReceivedEvent event) {
        try (PreparedStatement statement = DatabaseConnector.getConn()
                .prepareStatement("SELECT shepard_func.get_changelog_channel(?)")) {
            statement.setString(1, guild.getId());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString(1);
            }
        } catch (SQLException e) {
            handleExceptionAndIgnore(e, event);
        }
        return null;
    }
}
