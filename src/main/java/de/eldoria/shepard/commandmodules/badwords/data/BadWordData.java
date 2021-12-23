package de.eldoria.shepard.commandmodules.badwords.data;

import de.eldoria.shepard.database.DbUtil;
import de.eldoria.shepard.database.QueryObject;
import de.eldoria.shepard.wrapper.EventWrapper;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BadWordData extends QueryObject {
    /**
     * Default constructor for data object.
     *
     * @param source data source for information retrieval
     */
    public BadWordData(DataSource source) {
        super(source);
    }

    public boolean addBadword(long guildid, String word, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement stmt =
                conn.prepareStatement("SELECT shepard_func.add_badword(?, ?)")) {
            stmt.setLong(1, guildid);
            stmt.setString(2, word);
            return stmt.execute();
        } catch (SQLException e) {
            DbUtil.handleException(e, wrapper);
            return false;
        }
    }

    public boolean removeBadword(long guildid, String word, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement stmt =
                conn.prepareStatement("SELECT shepard_func.remove_badword(?, ?)")) {
            stmt.setLong(1, guildid);
            stmt.setString(2, word);
            return stmt.execute();
        } catch (SQLException e) {
            DbUtil.handleException(e, wrapper);
            return false;
        }
    }

    public String[] getList(long guildId, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement stmt =
                conn.prepareStatement("SELECT shepard_func.get_badwords(?)")) {
            stmt.setLong(1, guildId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Array resultarray = rs.getArray(1);
                if (resultarray != null) {
                    return (String[]) resultarray.getArray();
                } else {
                    return new String[] {};
                }

            } else {
                return new String[] {};
            }
        } catch (SQLException e) {
            DbUtil.handleException(e, wrapper);
            return null;
        }
    }
}
