package de.eldoria.shepard.reactionactions;

import de.eldoria.shepard.contexts.commands.Command;
import de.eldoria.shepard.messagehandler.ErrorType;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.util.reactions.Emoji;
import de.eldoria.shepard.wrapper.MessageEventDataWrapper;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

public class ExecuteCommand extends Action {

    private final Command command;
    private final String[] args;
    private final MessageEventDataWrapper messageContext;

    /**
     * Execute a command.
     *
     * @param exclusiveUser  user which is allowed to use this execution. null if everyone is allowed.
     * @param command        command to execute
     * @param args           command args
     * @param messageContext message context for execution
     */
    public ExecuteCommand(User exclusiveUser, Command command,
                          String[] args, MessageEventDataWrapper messageContext) {
        super(Emoji.CHECK_MARK_BUTTON, exclusiveUser, 60, true);
        this.command = command;
        this.args = args;
        this.messageContext = messageContext;
    }

    @Override
    protected void internalExecute(GuildMessageReactionAddEvent event) {
        if (command.checkArguments(args)) {
            command.executeAsync(command.getCommandName(), args, messageContext);
            event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
                if (message != null) {
                    message.delete().queue();
                }
            });
        } else {
            MessageSender.sendSimpleError(ErrorType.INVALID_ARGUMENT, messageContext.getTextChannel());
        }
    }
}