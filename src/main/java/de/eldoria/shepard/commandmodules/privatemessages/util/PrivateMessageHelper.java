package de.eldoria.shepard.commandmodules.privatemessages.util;

import de.eldoria.shepard.basemodules.commanddispatching.util.ArgumentParser;
import de.eldoria.shepard.commandmodules.privatemessages.commands.PrivateAnswer;
import de.eldoria.shepard.commandmodules.privatemessages.commands.SendPrivateMessage;
import de.eldoria.shepard.messagehandler.ErrorType;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.util.reactions.Emoji;
import de.eldoria.shepard.wrapper.EventWrapper;
import net.dv8tion.jda.api.entities.User;

/**
 * Helper methods which are used in {@link PrivateAnswer} and {@link SendPrivateMessage}.
 */
public final class PrivateMessageHelper {

    private PrivateMessageHelper() {
    }

    /**
     * Sends a private message to a user.
     *
     * @param args           arguments to send. Are joined with white spaces
     * @param messageContext message context for error handling
     * @param user           user which should receive the message
     */
    public static void sendPrivateMessage(String[] args, EventWrapper messageContext, User user) {
        if (user == null) {
            MessageSender.sendSimpleError(ErrorType.INVALID_USER, messageContext);
            return;
        }

        String text = ArgumentParser.getMessage(args, 1);

        MessageSender.sendAttachment(user, messageContext.getMessage().get().getAttachments(), text,
                messageContext.getMessageChannel());

        messageContext.getMessage().get().addReaction(Emoji.CHECK_MARK_BUTTON.unicode).queue();
    }
}
