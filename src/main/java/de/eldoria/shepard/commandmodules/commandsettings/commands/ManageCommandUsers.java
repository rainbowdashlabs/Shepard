package de.eldoria.shepard.commandmodules.commandsettings.commands;

import de.eldoria.shepard.basemodules.commanddispatching.util.ArgumentParser;
import de.eldoria.shepard.commandmodules.Command;
import de.eldoria.shepard.commandmodules.argument.Parameter;
import de.eldoria.shepard.commandmodules.argument.SubCommand;
import de.eldoria.shepard.commandmodules.command.CommandUsage;
import de.eldoria.shepard.commandmodules.command.Executable;
import de.eldoria.shepard.commandmodules.commandsettings.data.CommandData;
import de.eldoria.shepard.commandmodules.commandsettings.types.ListType;
import de.eldoria.shepard.commandmodules.commandsettings.types.ModifyType;
import de.eldoria.shepard.commandmodules.util.CommandCategory;
import de.eldoria.shepard.messagehandler.ErrorType;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.modulebuilder.requirements.ReqDataSource;
import de.eldoria.shepard.modulebuilder.requirements.ReqParser;
import de.eldoria.shepard.util.BooleanState;
import de.eldoria.shepard.wrapper.EventContext;
import de.eldoria.shepard.wrapper.EventWrapper;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.eldoria.shepard.localization.enums.commands.botconfig.ManageContextUserLocale.*;
import static de.eldoria.shepard.localization.util.TextLocalizer.localizeAllAndReplace;

/**
 * Manage the user settings of a registered and active {@link Command}.
 */
@CommandUsage(EventContext.GUILD)
public class ManageCommandUsers extends Command implements Executable, ReqParser, ReqDataSource {
    private ArgumentParser parser;
    private CommandData commandData;

    /**
     * Creates a new Manage context user command object.
     */
    public ManageCommandUsers() {
        super("manageCommandUser",
                new String[]{"mcu"},
                "command.manageContextUser.description",
                SubCommand.builder("manageCommandUser")
                        .addSubcommand("command.manageContextUser.subcommand.setActive",
                                Parameter.createCommand("setActive"),
                                Parameter.createInput("command.general.argument.contextName", "command.general.argumentDescription.contextName", true),
                                Parameter.createInput("command.general.argument.boolean", null, true))
                        .addSubcommand("command.manageContextUser.subcommand.setListType",
                                Parameter.createCommand("setListType"),
                                Parameter.createInput("command.general.argument.contextName", "command.general.argumentDescription.contextName", true),
                                Parameter.createInput("command.general.argument.listType", "command.general.argumentDescription.listType", true))
                        .addSubcommand("command.manageContextUser.subcommand.addUser",
                                Parameter.createCommand("addUser"),
                                Parameter.createInput("command.general.argument.contextName", "command.general.argumentDescription.contextName", true),
                                Parameter.createInput("command.general.argument.users", "command.general.argumentDescription.users", true))
                        .addSubcommand("command.manageContextUser.subcommand.removeUser",
                                Parameter.createCommand("removeUser"),
                                Parameter.createInput("command.general.argument.contextName", "command.general.argumentDescription.contextName", true),
                                Parameter.createInput("command.general.argument.users", "command.general.argumentDescription.users", true))
                        .build(),
                CommandCategory.BOT_CONFIG);
    }

    @Override
    public void execute(String label, String[] args, EventWrapper wrapper) {
        Optional<Command> command = parser.getCommand(args[1]);
        String cmd = args[0];

        if (command.isEmpty()) {
            MessageSender.sendSimpleError(ErrorType.COMMAND_SEARCH_EMPTY, wrapper);
            return;
        }

        if (isSubCommand(cmd, 0)) {
            setActive(args, command.get(), wrapper);
            return;
        }

        if (isSubCommand(cmd, 1)) {
            setListType(args, command.get(), wrapper);
            return;
        }

        if (isSubCommand(cmd, 2)) {
            addUser(args, command.get(), wrapper);
            return;
        }

        if (isSubCommand(cmd, 3)) {
            removeUser(args, command.get(), wrapper);
            return;
        }
    }

    private void manageUser(String[] args, Command context,
                            ModifyType modifyType, EventWrapper wrapper) {
        List<String> mentions = new ArrayList<>();

        parser.getGuildUsers(wrapper.getGuild().get(),
                ArgumentParser.getRangeAsList(args, 2))
                .forEach(user -> {
                    if (modifyType == ModifyType.ADD) {
                        if (!commandData.addUser(context, user, wrapper)) {
                            return;
                        }
                    } else if (!commandData.removeUser(context, user, wrapper)) {
                        return;
                    }
                    mentions.add(user.getAsMention());
                });

        String names = String.join(System.lineSeparator(), mentions);

        if (modifyType == ModifyType.ADD) {
            MessageSender.sendSimpleTextBox(localizeAllAndReplace(M_ADDED_USERS.tag, wrapper,
                    " **" + context.getCommandName().toUpperCase() + "**"), names, wrapper);

        } else {
            MessageSender.sendSimpleTextBox(localizeAllAndReplace(M_REMOVED_USERS.tag, wrapper,
                    " **" + context.getCommandName().toUpperCase() + "**"), names, wrapper);
        }
    }

    private void addUser(String[] args, Command context, EventWrapper wrapper) {
        manageUser(args, context, ModifyType.ADD, wrapper);
    }

    private void removeUser(String[] args, Command context, EventWrapper wrapper) {
        manageUser(args, context, ModifyType.REMOVE, wrapper);
    }

    private void setListType(String[] args, Command context, EventWrapper wrapper) {
        ListType type = ListType.getType(args[2]);

        if (type == null) {
            MessageSender.sendSimpleError(ErrorType.INVALID_LIST_TYPE, wrapper);
            return;
        }

        if (commandData.setUserListType(context, type, wrapper)) {
            MessageSender.sendMessage(localizeAllAndReplace(M_CHANGED_LIST_TYPE.tag,
                    wrapper, "**" + context.getCommandName().toUpperCase() + "**",
                    "**" + type.toString() + "**"), wrapper.getMessageChannel());
        }
    }

    private void setActive(String[] args, Command context, EventWrapper wrapper) {
        BooleanState bState = ArgumentParser.getBoolean(args[2]);

        if (bState == BooleanState.UNDEFINED) {
            MessageSender.sendSimpleError(ErrorType.INVALID_BOOLEAN, wrapper);
            return;
        }

        boolean state = bState.stateAsBoolean;

        if (!commandData.setUserCheckActive(context, state, wrapper)) {
            return;
        }

        if (state) {
            MessageSender.sendMessage(localizeAllAndReplace(M_ACTIVATED_CHECK.tag, wrapper,
                    "**" + context.getCommandName().toUpperCase() + "**"), wrapper.getMessageChannel());
        } else {
            MessageSender.sendMessage(localizeAllAndReplace(M_DEACTIVATED_CHECK.tag, wrapper,
                    "**" + context.getCommandName().toUpperCase() + "**"), wrapper.getMessageChannel());
        }
    }

    @Override
    public void addParser(ArgumentParser parser) {
        this.parser = parser;
    }

    @Override
    public void addDataSource(DataSource source) {
        commandData = new CommandData(source);
    }

}
