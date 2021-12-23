package de.eldoria.shepard.database;

import de.eldoria.shepard.database.types.Rank;
import de.eldoria.shepard.messagehandler.ErrorType;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.wrapper.EventWrapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public final class DbUtil {
    private static final Pattern ID_PATTERN = Pattern.compile("(?:<[@#!&]{1,2})?(?<id>[0-9]{18})(?:>)?");

    private DbUtil() {
    }

    /**
     * Get a sorted ranked list from a result set.
     *
     * @param shardManager    jda instance
     * @param result Result set to retrieve ranks.
     * @return List of ranks.
     * @throws SQLException SQL exception
     */
    public static List<Rank> getScoreListFromResult(ShardManager shardManager, ResultSet result) throws SQLException {
        List<Rank> ranks = new ArrayList<>();

        while (result.next()) {
            User user = shardManager.getUserById(result.getString("user_id"));
            if (user != null) {
                ranks.add(new Rank(user, result.getInt("score")));
            }
        }
        return ranks;
    }


    /**
     * Extracts an id from discord's formatting.
     *
     * @param id the formatted id.
     * @return the extracted id.
     */
    public static String getIdRaw(String id) {
        Matcher matcher = ID_PATTERN.matcher(id);
        if (!matcher.matches()) {
            return "0";
        }
        return matcher.group(1);
    }

    /**
     * Handles SQL Exceptions and throws it.
     *
     * @param ex    SQL Exception
     * @param wrapper Event for error sending to channel to inform user.
     */
    public static void handleException(SQLException ex, EventWrapper wrapper) {

        String builder = "SQLException: " + ex.getMessage() + "\n"
                + "SQLState: " + ex.getSQLState() + "\n"
                + "VendorError: " + ex.getErrorCode();
        log.error(builder, ex);

        if (wrapper != null) {
            MessageSender.sendSimpleError(ErrorType.DATABASE_ERROR, wrapper);
        }
    }

    /**
     * Get a array of snowflakes from a list.
     *
     * @param snowflakes list of snowflake objects
     * @param conn       connection
     * @return array of snowflakes as bigint array
     * @throws SQLException when the creation failed
     */
    public static Array getSnowflakeArray(List<?> snowflakes, Connection conn) throws SQLException {
        Long[] userIds = new Long[snowflakes.size()];
        snowflakes.stream().map(c -> ((ISnowflake) c).getIdLong()).collect(Collectors.toList()).toArray(userIds);
        return conn.createArrayOf("bigint", userIds);
    }
}
