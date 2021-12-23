package de.eldoria.shepard.commandmodules.kudos.data;

import de.eldoria.shepard.database.QueryObject;
import de.eldoria.shepard.database.types.Rank;
import de.eldoria.shepard.wrapper.EventWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static de.eldoria.shepard.database.DbUtil.getScoreListFromResult;
import static de.eldoria.shepard.database.DbUtil.handleException;

public final class KudoData extends QueryObject {
    /**
     * Create a new kudo data object.
     *
     * @param source data source for connection retrieval
     */
    public KudoData(DataSource source) {
        super(source);
    }

    /**
     * Try to take the points from the user.
     *
     * @param guild          guild where the points should be taken.
     * @param user           user from who the points should be taken.
     * @param points         points to take
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return true if the points where taken.
     */
    public boolean tryTakePoints(Guild guild, User user, int points, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.try_take_rubber_points(?,?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.setInt(3, points);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getBoolean(1);
            }
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return false;
    }

    /**
     * Try to take the points from the user. Uses the free kudos first.
     *
     * @param guild          guild where the points should be taken.
     * @param user           user from who the points should be taken.
     * @param points         points to take
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return true if the points where taken.
     */
    public boolean tryTakeCompletePoints(Guild guild, User user, int points,
                                         EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.try_take_complete_rubber_points(?,?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.setInt(3, points);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getBoolean(1);
            }
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return false;
    }

    /**
     * Add the score to the score in the database. Negative score subtracts from score.
     *
     * @param guild          Guild where the score should be applied
     * @param user           user where the score should be applied
     * @param score          The score which should be applied
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return true if the query execution was successful
     */
    public boolean addRubberPoints(Guild guild, User user, int score,
                                   EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT shepard_func.add_rubber_points(?,?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.setInt(3, score);
            statement.execute();
        } catch (SQLException e) {
            handleException(e, wrapper);
            return false;
        }
        return true;
    }

    /**
     * Add the score to the score in the database. Negative score subtracts from score.
     *
     * @param user           user where the score should be applied
     * @param score          The score which should be applied
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return true if the query execution was successful
     */
    public boolean addRubberPoints(User user, int score,
                                   EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT shepard_func.add_rubber_points(?,?)")) {
            statement.setString(1, user.getId());
            statement.setInt(2, score);
            statement.execute();
        } catch (SQLException e) {
            handleException(e, wrapper);
            return false;
        }
        return true;
    }

    /**
     * Add the score to the jackpot in the database.
     *
     * @param guild          guild where the score should be applied
     * @param score          The score which should be applied
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return true if the query execution was successful
     */
    public int addAndGetJackpot(Guild guild, int score,
                                EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT shepard_func.add_and_get_jackpot(?,?)")) {
            statement.setString(1, guild.getId());
            statement.setInt(2, score);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            handleException(e, wrapper);
            return 0;
        }
        return 0;
    }


    /**
     * Add the amount to the amount in the database. Negative amount subtracts from amount.
     *
     * @param guild          Guild where the amount should be applied
     * @param user           user where the amount should be applied
     * @param amount         The amount which should be applied
     * @param wrapper wrapper from command sending for error handling. Can be null.
     */
    public void addFreeRubberPoints(Guild guild, User user, int amount,
                                    EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT shepard_func.add_free_rubber_points(?,?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.setInt(3, amount);
            statement.execute();
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
    }

    /**
     * Add the amount to the amount in the database. Negative amount subtracts from amount.
     *
     * @param user           user where the amount should be applied
     * @param amount         The amount which should be applied
     * @param wrapper wrapper from command sending for error handling. Can be null.
     */
    public void addFreeRubberPoints(User user, int amount,
                                    EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT shepard_func.add_free_rubber_points(?,?)")) {
            statement.setString(1, user.getId());
            statement.setInt(2, amount);
            statement.execute();
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
    }

    /**
     * Get the top x on a guild.
     *
     * @param guild          Guild where you want to have the top x
     * @param scoreAmount    Amount of entries. For the top 10 enter a 10.
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @param jda            jda instance
     * @return sorted list of ranks in descending order.
     */
    public List<Rank> getTopScore(Guild guild, int scoreAmount, EventWrapper wrapper, ShardManager jda) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.get_rubber_points_top_score(?,?)")) {
            statement.setString(1, guild.getId());
            statement.setInt(2, scoreAmount);
            return getScoreListFromResult(jda, statement.executeQuery());
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return Collections.emptyList();
    }

    /**
     * Get the global top x. The score of all guilds is accumulated for each user.
     *
     * @param scoreAmount    Amount of entries. For the top 10 enter a 10.
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @param jda            jda instance
     * @return sorted list of ranks in descending order.
     */
    public List<Rank> getGlobalTopScore(int scoreAmount, EventWrapper wrapper, ShardManager jda) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.get_rubber_points_global_top_score(?)")) {
            statement.setInt(1, scoreAmount);
            return getScoreListFromResult(jda, statement.executeQuery());
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return Collections.emptyList();
    }

    /**
     * Get the score of a user on a guild.
     *
     * @param guild          Guild for lookup
     * @param user           User for lookup
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return score of user.
     */
    public int getUserScore(Guild guild, User user, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.get_rubber_points_user_score(?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return -1;
    }

    /**
     * Get the score of a user on a guild.
     *
     * @param guild          Guild for lookup
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return score of user.
     */
    public int getAndClearJackpot(Guild guild, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.get_and_clear_jackpot(?)")) {
            statement.setString(1, guild.getId());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }
        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return -1;
    }

    /**
     * Get the sum of all scores of a user.
     *
     * @param user           User for lookup.
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return global score of user
     */
    public int getGlobalUserScore(User user, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.get_rubber_points_global_user_score(?)")) {
            statement.setString(1, user.getId());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return -1;
    }

    /**
     * Get the sum of all scores of a user.
     *
     * @param guild          Guild for lookup
     * @param user           User for lookup.
     * @param wrapper wrapper from command sending for error handling. Can be null.
     * @return global score of user
     */
    public int getFreePoints(Guild guild, User user, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT * from shepard_func.get_free_rubber_points(?,?)")) {
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            handleException(e, wrapper);
        }
        return -1;
    }

    /**
     * Add to all users 1 kudo.
     */
    public void upcountKudos() {
        try (var conn = source.getConnection(); PreparedStatement statement = conn
                .prepareStatement("SELECT shepard_func.upcount_free_rubber_points()")) {
            statement.execute();
        } catch (SQLException e) {
            handleException(e, null);
        }
    }


}