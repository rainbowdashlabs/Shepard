package de.eldoria.shepard.commandmodules.argument;

import de.eldoria.shepard.webapi.apiobjects.commandserialization.ParameterInfo;
import de.eldoria.shepard.webapi.apiobjects.commandserialization.SubCommandInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An argument of a command.
 * An CommandArg is the highest instance to define the arguments of a command.
 */
public final class SubCommand {
    private static final SubCommand WILDCARD = new SubCommand("wildcard", "",
            Parameter.createCommand("*"));
    private static final SubCommand BASE = new SubCommand("base", "",
            Parameter.createCommand("base"));
    private static final SubCommand STANDALONE = new SubCommand("standalone", "",
            Parameter.createCommand("standalone"));

    // Short description of the sub command
    private final String commandDescription;
    private final Parameter[] parameters;
    private final String commandName;
    private String commandPattern;

    /**
     * Create a new argument with a name, description and whether it is required or not.
     *
     * @param commandName        the name of the argument.
     * @param commandDescription Description what the sub command does.
     * @param parameters         parameters of the subcommand. order matters.
     */
    private SubCommand(String commandName, String commandDescription, Parameter... parameters) {
        this.commandName = commandName;
        this.commandDescription = commandDescription;
        this.parameters = parameters;
    }


    /**
     * Argument name with subarguments.
     *
     * @param commandName command name for pattern generation
     * @return string with more information
     */
    private String generateCommandPatternHelp(String commandName) {
        List<String> params = new ArrayList<>();
        List<String> paramsDescription = new ArrayList<>();
        for (Parameter p : parameters) {
            // check if command.
            if (p.isCommand()) {
                params.add(p.getCommandName() + "|" + p.getShortCommand());
            } else {
                params.add(p.isRequired() ? "<$" + p.getInputName() + "$>" : "[$" + p.getInputName() + "$]");
                if (p.getInputDescription() != null) {
                    paramsDescription.add(p.getInputDesc());
                }
            }
        }
        return (commandDescription != null ? "*__$" + (commandDescription) + "$__*\n" : "")
                + "`{prefix}" + commandName + " " + String.join(" ", params) + "`"
                + (paramsDescription.isEmpty() ? "" : "\n" + String.join("\n", paramsDescription));
    }

    /**
     * Checks if a string matches the command or alias of a subcommand.
     *
     * @param cmd command to check
     * @return true if the command or alias matches. case ignore
     */
    public boolean isSubCommand(String cmd) {
        for (var p : parameters) {
            if (p.isCommand()) {
                return p.isCommandParameter(cmd);
            }
        }
        return false;
    }

    /**
     * Get the sub arguments of the argument.
     *
     * @return array of sub arguments Is empty when no arguments are set.
     */
    public Parameter[] getParameters() {
        return parameters;
    }

    /**
     * Get the command pattern of subcommand.
     *
     * @return commandpattern as preformatted string
     */
    public String getCommandPattern() {
        if (commandPattern == null) {
            commandPattern = generateCommandPatternHelp(commandName);
        }
        return commandPattern;
    }

    /**
     * Check if the args match this subcommand.
     *
     * @param args args to check
     * @return true if all commands are present in string and enough parameter are present.
     */
    public boolean matchArgs(String[] args) {
        if (args.length == 0) return false;
        boolean match = true;
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            if (args.length > i) {
                match = p.isCommandParameter(args[i]);
            } else {
                if (p.isRequired()) {
                    match = false;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the sub command info object of the subcommand.
     *
     * @return new sub command info of the command.
     */
    public SubCommandInfo getSubCommandInfo() {
        ParameterInfo[] pI = new ParameterInfo[parameters.length];
        return new SubCommandInfo(commandDescription,
                Arrays.stream(parameters)
                        .map(Parameter::getParameterInfo)
                        .collect(Collectors.toList())
                        .toArray(pI));
    }

    /**
     * Get the subcommand identifier.
     * This will be the first subcommand or "base" if the subcommand does not have a command input
     *
     * @return subcommand identifier as string.
     */
    public String getSubCommandIdentifier() {
        for (var p : parameters) {
            if (p.isCommand()) {
                return p.getCommandName();
            }
        }
        return BASE.getSubCommandIdentifier();
    }

    /**
     * Get a new Subcommand builder.
     *
     * @param commandName command name for builder
     * @return new command builder object
     */
    public static SubCommandBuilder builder(String commandName) {
        return new SubCommandBuilder(commandName);
    }

    /**
     * Get the standalone subcommand.
     *
     * @return static standalone instance
     */
    public static SubCommand standalone() {
        return STANDALONE;
    }

    /**
     * Get the base subcommand.
     *
     * @return static base instance
     */
    public static SubCommand base() {
        return BASE;
    }

    /**
     * Get the wildcard subcommand.
     *
     * @return static wildcard instance
     */
    public static SubCommand wildcard() {
        return WILDCARD;
    }

    public static final class SubCommandBuilder {
        private final List<SubCommand> subCommands = new ArrayList<>();
        private final String commandName;

        private SubCommandBuilder(String commandName) {
            this.commandName = commandName;
        }

        /**
         * Add a subcommand to the sub command builder.
         *
         * @param description description of subcommand
         * @param parameters  parameter of subcommand
         * @return self instance with added subcommand.
         */
        public SubCommandBuilder addSubcommand(String description, Parameter... parameters) {
            subCommands.add(new SubCommand(commandName, description, parameters));
            return this;
        }

        /**
         * build the sub command array from entered subcommands.
         *
         * @return array of subcommand
         */
        public SubCommand[] build() {
            SubCommand[] sc = new SubCommand[subCommands.size()];
            return subCommands.toArray(sc);
        }
    }
}

