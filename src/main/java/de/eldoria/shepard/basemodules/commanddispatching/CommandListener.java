package de.eldoria.shepard.basemodules.commanddispatching;

import de.eldoria.shepard.basemodules.commanddispatching.dialogue.DialogHandler;
import de.eldoria.shepard.basemodules.commanddispatching.util.ExecutionValidator;
import de.eldoria.shepard.basemodules.reactionactions.ReactionActionCollection;
import de.eldoria.shepard.basemodules.reactionactions.actions.ExecuteCommand;
import de.eldoria.shepard.basemodules.reactionactions.actions.SendCommandHelp;
import de.eldoria.shepard.commandmodules.Command;
import de.eldoria.shepard.commandmodules.prefix.PrefixData;
import de.eldoria.shepard.core.Statistics;
import de.eldoria.shepard.core.configuration.Config;
import de.eldoria.shepard.database.DbUtil;
import de.eldoria.shepard.localization.util.LocalizedEmbedBuilder;
import de.eldoria.shepard.localization.util.LocalizedField;
import de.eldoria.shepard.localization.util.TextLocalizer;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.messagehandler.ShepardReactions;
import de.eldoria.shepard.modulebuilder.requirements.*;
import de.eldoria.shepard.util.Verifier;
import de.eldoria.shepard.wrapper.EventWrapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.eldoria.shepard.localization.enums.listener.CommandListenerLocale.*;

@Slf4j
public class CommandListener extends ListenerAdapter
        implements ReqCommands, ReqExecutionValidator, ReqReactionAction, ReqDataSource, ReqConfig, ReqStatistics, ReqDialogHandler {
    private CommandHub commands;
    private ExecutionValidator executionValidator;
    private ReactionActionCollection reactionAction;
    private PrefixData prefixData;
    private Config config;
    private Statistics statistics;
    private DialogHandler dialogHandler;

    /**
     * Create a new command listener.
     */
    public CommandListener() {
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        onCommand(EventWrapper.wrap(event));
    }

    @Override
    public void onPrivateMessageUpdate(@Nonnull PrivateMessageUpdateEvent event) {
        if (event.getMessage().getTimeCreated().isAfter(OffsetDateTime.now().minusMinutes(5))) {
            onCommand(EventWrapper.wrap(event));
        }
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        if (event.getMessage().getTimeCreated().isAfter(OffsetDateTime.now().minusMinutes(5))) {
            onCommand(EventWrapper.wrap(event));
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        onCommand(EventWrapper.wrap(event));
    }

    /**
     * Checks if the message is a Command on the guild.
     * Suggests a command if no command is valid and some similar command are found.
     * Parses the input into commands.
     *
     * @param eventWrapper context to check
     */
    private void onCommand(EventWrapper eventWrapper) {
        statistics.eventDispatched(eventWrapper.getJDA());

        String receivedMessage = eventWrapper.getMessage().get().getContentRaw();
        receivedMessage = receivedMessage.replaceAll("\\s\\s+", " ");
        String[] args = receivedMessage.split(" ");

        // Check if message is command
        if (!isCommand(receivedMessage, args, eventWrapper)) {
            if (eventWrapper.getMessage().isPresent()) {
                dialogHandler.invoke(eventWrapper, eventWrapper.getMessage().get());
            }
            return;
        }

        statistics.commandDispatched(eventWrapper.getJDA());

        // Ignore if the command is send by shepard
        if (Verifier.equalSnowflake(eventWrapper.getAuthor(), eventWrapper.getJDA().getSelfUser())
                || eventWrapper.getAuthor().isBot()) {
            return;
        }

        args = stripArgs(receivedMessage, args, eventWrapper);
        String label = args[0];
        args = buildArgs(args);

        // Find the executed command.
        Optional<Command> command = commands.getCommand(label);

        if (command.isPresent()) {
            commands.dispatchCommand(command.get(), label, args, eventWrapper);
            return;
        }
        String prefix = config.getGeneralSettings().getPrefix();
        if (eventWrapper.isGuildEvent()) {
            prefix = prefixData.getPrefix(eventWrapper.getGuild().get());
        }

        if (searchAndSendSuggestion(eventWrapper, args, label, prefix)) return;

        MessageSender.sendError(config,
                new LocalizedField[]{
                        new LocalizedField(M_COMMAND_NOT_FOUND.tag, TextLocalizer.localizeAllAndReplace(M_HELP_COMMAND.tag,
                                eventWrapper, "`" + prefix + "help`"), false, eventWrapper)},
                eventWrapper);
    }

    private boolean searchAndSendSuggestion(EventWrapper eventWrapper,
                                            String[] args, String label, String prefix) {
        List<Command> similarCommand = commands.getSimilarCommands(label);
        if (!similarCommand.isEmpty()) {
            for (Command cmd : similarCommand) {
                if (!executionValidator.canAccess(cmd, eventWrapper)) continue;
                LocalizedEmbedBuilder builder = new LocalizedEmbedBuilder(eventWrapper)
                        .setTitle(M_COMMAND_NOT_FOUND.tag)
                        .setColor(Color.green)
                        .setDescription(M_SUGGESTION + System.lineSeparator() + "**" + cmd.getCommandName() + "**")
                        .setThumbnail(ShepardReactions.WINK.thumbnail);
                try {
                    eventWrapper.getMessageChannel().sendMessage(builder.build()).queue(m -> {
                        if (eventWrapper.isGuildEvent()) {
                            reactionAction.addReactionAction(
                                    m, new ExecuteCommand(commands, eventWrapper.getAuthor(), cmd, args, eventWrapper),
                                    new SendCommandHelp(cmd, prefix));
                        }
                    });
                } catch (InsufficientPermissionException e) {
                    MessageSender.handlePermissionException(config, e, eventWrapper);
                }
                return true;
            }
        }
        return false;
    }


    @NotNull
    private String[] buildArgs(String[] args) {
        String[] newArgs;
        if (args.length > 1) {
            newArgs = Arrays.copyOfRange(args, 1, args.length);
        } else {
            newArgs = new String[0];
        }
        return newArgs;
    }

    private boolean isCommand(String receivedMessage, String[] args, EventWrapper eventWrapper) {
        boolean isCommand = false;
        String prefix = prefixData.getPrefix(eventWrapper);
        if (receivedMessage.startsWith(prefix)) {
            isCommand = true;
            //Check if the message is a command executed by a mention of the bot.
        } else if (DbUtil.getIdRaw(args[0]).contentEquals(eventWrapper.getJDA().getSelfUser().getId())) {
            isCommand = true;
        }
        return isCommand;
    }

    private String[] stripArgs(String receivedMessage, String[] args, EventWrapper messageContext) {
        String prefix = prefixData.getPrefix(messageContext);
        String[] strippedArgs;
        if (receivedMessage.startsWith(prefix)) {
            args[0] = args[0].substring(prefix.length());
            strippedArgs = args;
            //Check if the message is a command executed by a mention of the bot.
        } else if (DbUtil.getIdRaw(args[0]).contentEquals(messageContext.getJDA().getSelfUser().getId())) {
            strippedArgs = Arrays.copyOfRange(args, 1, args.length);
        } else {
            strippedArgs = args;
        }
        return strippedArgs;
    }


    @Override
    public void addCommands(CommandHub commandHub) {
        this.commands = commandHub;
    }

    @Override
    public void addExecutionValidator(ExecutionValidator validator) {
        this.executionValidator = validator;
    }

    @Override
    public void addReactionAction(ReactionActionCollection reactionAction) {
        this.reactionAction = reactionAction;
    }

    @Override
    public void addConfig(Config config) {
        this.config = config;
    }

    @Override
    public void addDataSource(DataSource source) {
        prefixData = new PrefixData(source, config);
    }

    @Override
    public void addStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public void addDialogHandler(DialogHandler dialogHandler) {
        this.dialogHandler = dialogHandler;
    }
}

