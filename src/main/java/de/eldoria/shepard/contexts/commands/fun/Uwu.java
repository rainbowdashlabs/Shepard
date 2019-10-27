package de.eldoria.shepard.contexts.commands.fun;

import de.eldoria.shepard.contexts.ContextCategory;
import de.eldoria.shepard.localization.enums.fun.UwuLocale;
import de.eldoria.shepard.wrapper.MessageEventDataWrapper;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.contexts.commands.Command;

import static de.eldoria.shepard.localization.enums.fun.UwuLocale.DESCRIPTION;

public class Uwu extends Command {

    /**
     * Creates new uwu command object.
     */
    public Uwu() {
        commandName = "uwu";
        commandDesc = DESCRIPTION.replacement;
        commandAliases = new String[] {"uwud"};
        category = ContextCategory.FUN;
    }


    @Override
    protected void internalExecute(String label, String[] args, MessageEventDataWrapper messageContext) {
        MessageSender.sendMessage(":regional_indicator_u::regional_indicator_w::regional_indicator_u:",
                messageContext);

        if (label.equalsIgnoreCase("uwud")) {
            messageContext.getMessage().delete().queue();
        }
    }
}
