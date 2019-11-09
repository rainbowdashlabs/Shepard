package de.eldoria.shepard.contexts.keywords.keyword;

import de.eldoria.shepard.ShepardBot;
import de.eldoria.shepard.contexts.keywords.Keyword;
import de.eldoria.shepard.messagehandler.MessageSender;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Objects;

public class Thing extends Keyword {

    /**
     * Creates a new think keyword object.
     */
    public Thing() {
        keywords = new String[] {"ding"};
    }

    @Override
    public void execute(GuildMessageReceivedEvent event, String key) {
        MessageSender.sendMessage(event.getMessage().getContentRaw() + " by <@" + event.getAuthor().getId() + ">",
                Objects.requireNonNull(ShepardBot.getJDA().getGuildById("336473392863510538"))
                        .getTextChannelById("538103926126280706"));
    }
}
