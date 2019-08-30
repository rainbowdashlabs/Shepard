package de.eldoria.shepard.contexts.keywords.keyword;

import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.contexts.keywords.Keyword;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommanderQuestion extends Keyword {

    /**
     * Creates a new Commander Question keyword object.
     */
    public CommanderQuestion() {
        keywords = new String[] {"commander?", "shepard?"};
    }

    @Override
    public void execute(MessageReceivedEvent event, String key) {
        if (key.equalsIgnoreCase(keywords[0])) {
            MessageSender.sendMessage("Commander Shepard meldet sich zum Dienst!", event.getChannel());
        }
        if (key.equalsIgnoreCase(keywords[1])) {
            MessageSender.sendMessage("Hier bin ich o/", event.getChannel());
        }
    }
}
