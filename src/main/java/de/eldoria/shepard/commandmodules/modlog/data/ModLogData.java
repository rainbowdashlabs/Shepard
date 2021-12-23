package de.eldoria.shepard.commandmodules.modlog.data;

import de.eldoria.shepard.database.DbUtil;
import de.eldoria.shepard.database.QueryObject;
import de.eldoria.shepard.wrapper.EventWrapper;
import net.dv8tion.jda.api.entities.Guild;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalLong;


public final class ModLogData extends QueryObject {
    /**
     * Default constructor for data object.
     *
     * @param source data source for information retrieval
     */
    public ModLogData(DataSource source) {
        super(source);
    }



    public boolean updateOrSetModChannel(long guildId, long channelId, EventWrapper wrapper) {
        try(var conn= source.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT shepard_func.set_modLog(?,?)"
        )){
            stmt.setLong(1, guildId);
            stmt.setLong(2, channelId);
            stmt.execute();
        }
        catch (SQLException e){
            DbUtil.handleException(e, wrapper);
            return false;
        }
        return true;
    }


    public boolean deleteModChannel(long guildId, EventWrapper wrapper){
        try(var conn= source.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT shepard_func.delete_modlog(?)"
        )){
            stmt.setLong(1, guildId);
            stmt.execute();
        }
        catch (SQLException e){
            DbUtil.handleException(e, wrapper);
            return false;
        }
        return true;
    }

    public OptionalLong getChannel(Guild guild, EventWrapper wrapper) {
        try (var conn = source.getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT shepard_func.get_modlog(?)"
        )) {
            stmt.setLong(1, guild.getIdLong());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return OptionalLong.of(rs.getLong(1));
            } else {
                return OptionalLong.empty();
            }
        } catch (SQLException e) {
            DbUtil.handleException(e, wrapper);
            return OptionalLong.empty();
        }
    }
}
