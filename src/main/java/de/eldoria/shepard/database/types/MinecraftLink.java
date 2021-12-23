package de.eldoria.shepard.database.types;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;

public class MinecraftLink {
    private final User user;
    private final String uuid;

    /**
     * Creates a new Minecraft link object.
     *
     * @param jda jda for user retrieval
     * @param userId id of user
     * @param uuid   minecraft id
     */
    public MinecraftLink(JDA jda, String userId, String uuid) {
        this.uuid = uuid;

        user = jda.getUserById(userId);
    }

    /**
     * Creates a new Minecraft link object.
     *
     * @param user user object
     * @param uuid minecraft id
     */
    public MinecraftLink(User user, String uuid) {
        this.user = user;
        this.uuid = uuid;
    }

    /**
     * Gets user of link.
     *
     * @return User object. Can be null.
     */
    @Nullable
    public User getUser() {
        return user;
    }

    /**
     * Get uuid of link.
     *
     * @return uuid as link. Can be null.
     */
    public String getUuid() {
        return uuid;
    }
}
