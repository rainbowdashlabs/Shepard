package de.chojo.shepard.contexts.commands;

import de.chojo.shepard.collections.CommandCollection;
import de.chojo.shepard.messagehandler.MessageSender;
import de.chojo.shepard.contexts.ContextSensitive;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An abstract class for commands.
 */
public abstract class Command extends ContextSensitive {
    /**
     * Name of the command.
     */
    protected String commandName = "";
    /**
     * Command aliase as string array.
     */
    protected String[] commandAliases = new String[0];
    /**
     * Description of command.
     */
    protected String commandDesc = "";
    /**
     * Command args as command arg array.
     */
    protected CommandArg[] arguments = new CommandArg[0];

    /**
     * Create a new command an register it to the {@link CommandCollection}.
     */
    protected Command() {
        CommandCollection.getInstance().addCommand(this);
    }

    /**
     * Executes the command.
     *
     * @param label         Label/Alias which was used for command execution
     * @param args          Arguments of the command.
     * @param receivedEvent Message Received Event of the command execution
     * @return True if the command was executed successful
     */
    public abstract boolean execute(String label, String[] args, MessageReceivedEvent receivedEvent);

    /**
     * Get the name of the command.
     *
     * @return the name of the command.
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Get the description of the command.
     *
     * @return the description
     */
    public String getCommandDesc() {
        return commandDesc;
    }


    /**
     * Get all possible arguments for the command.
     *
     * @return an array of command arguments.
     */
    private CommandArg[] getArguments() {
        if (arguments == null) {
            arguments = new CommandArg[0];
        }
        return arguments;
    }

    /**
     * Get possible aliases of a command.
     *
     * @return an array of aliases.
     */
    public String[] getCommandAliases() {
        return commandAliases;
    }

    /**
     * Check whether a string is a valid command or not.
     *
     * @param command the string to check.
     * @return {@code true} if the command matched, {@code false} otherwise.
     */
    public boolean isCommand(String command) {
        if (command.equalsIgnoreCase(this.commandName) || command.equalsIgnoreCase(getClass().getSimpleName())) {
            return true;
        }

        if (this.commandAliases != null) {
            for (String alias : this.commandAliases) {
                if (command.equalsIgnoreCase(alias)) return true;
            }
        }
        return false;
    }

    /**
     * Checks if enough arguments are present for the comment.
     *
     * @param args string arg array
     * @return true if enough arguments are present
     */
    public boolean checkArguments(String[] args) {
        int requiredArguments = 0;
        for (CommandArg a : arguments) {
            if (a.isRequired()) {
                requiredArguments++;
            }
        }
        return args.length >= requiredArguments;
    }

    /**
     * Send the usage of the command to a channel.
     *
     * @param channel Channel where the usage should be send in.
     */
    public void sendCommandUsage(MessageChannel channel) {
        List<MessageEmbed.Field> fields = new ArrayList<>();

        fields.add(new MessageEmbed.Field(getCommandDesc(), "", false));

        if (getCommandAliases() != null && getCommandAliases().length != 0) {
            fields.add(new MessageEmbed.Field("__**Aliases:**__", String.join(", ", getCommandAliases()), false));
        }


        StringBuilder desc = new StringBuilder();

        desc.append(getCommandName() + " ");
        if (getArguments() != null) {
            for (CommandArg arg : getArguments()) {
                if (arg.isRequired()) {
                    desc.append("[" + arg.getArgName().toUpperCase() + "] ");
                } else {
                    desc.append("<" + arg.getArgName().toUpperCase() + "> ");
                }
            }
        }

        fields.add(new MessageEmbed.Field("__**Usage:**__", desc.toString(), false));

        desc.setLength(0);
        if (arguments.length != 0) {

            for (CommandArg arg : arguments) {
                desc.append("**").append(arg.getArgName().toUpperCase()).append("**")
                        .append(arg.isRequired() ? " REQUIRED" : " OPTIONAL")
                        .append(System.lineSeparator())
                        .append("> ").append(arg.getArgDesc()
                        .replace(System.lineSeparator(), System.lineSeparator() + "> "))
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
            fields.add(new MessageEmbed.Field("__**Arguments:**__", desc.toString(), false));
        }


        MessageSender.sendTextBox("__**Help for command " + getCommandName() + "**__", fields, channel, Color.green);
    }

    /**
     * Sends help for a specified command argument.
     *
     * @param argument Argument for which should be send some detailed informations
     * @param channel  Channel where the usage should be send in.
     */
    public void sendCommandArgHelp(String argument, MessageChannel channel) {

        if (arguments == null || arguments.length == 0) {
            MessageSender.sendError(new MessageEmbed.Field[] {new MessageEmbed.Field("No Argument found!",
                    "This command, doesn't have any arguments.", false)}, channel);
            return;
        }

        for (CommandArg arg : arguments) {
            if (arg.getArgName().equalsIgnoreCase(argument)) {
                List<MessageEmbed.Field> fields = new ArrayList<>();
                fields.add(new MessageEmbed.Field("Description:", arg.getArgDesc(), false));
                fields.add(new MessageEmbed.Field("Required", arg.isRequired() ? "true" : "false", false));
                MessageSender.sendTextBox("Help for Argument: \"" + arg.getArgName() + "\" of command \""
                        + getCommandName() + "\"", fields, channel);
                return;
            }
        }

        String argsAsString =  Arrays.stream(arguments).map(arg -> arg.getArgName()).collect(Collectors.joining(" "));

        MessageSender.sendError(new MessageEmbed.Field[] {new MessageEmbed.Field("Argument not found!",
                "Try one of these: " + argsAsString, false)}, channel);
    }
}