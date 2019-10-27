package de.eldoria.shepard.contexts.commands.fun;

import de.eldoria.shepard.contexts.ContextCategory;
import de.eldoria.shepard.contexts.commands.Command;
import de.eldoria.shepard.contexts.commands.argument.CommandArg;
import de.eldoria.shepard.contexts.commands.argument.SubArg;
import de.eldoria.shepard.database.queries.QuoteData;
import de.eldoria.shepard.database.types.QuoteElement;
import de.eldoria.shepard.localization.enums.fun.QuoteLocale;
import de.eldoria.shepard.wrapper.MessageEventDataWrapper;
import de.eldoria.shepard.messagehandler.MessageSender;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static de.eldoria.shepard.localization.enums.fun.QuoteLocale.*;

public class Quote extends Command {

    /**
     * Create a new quote command object.
     */
    public Quote() {
        commandName = "Quote";
        commandDesc = DESCRIPTION.replacement;
        commandArgs = new CommandArg[] {
                new CommandArg("keyword", false,
                        new SubArg("keyword", A_EMPTY_OR_WORD.replacement))
        };
        category = ContextCategory.FUN;
    }

    @Override
    protected void internalExecute(String label, String[] args, MessageEventDataWrapper messageContext) {
        List<QuoteElement> quotes;
        if (args.length > 0) {
            quotes = QuoteData.getQuotesByKeyword(messageContext.getGuild(),
                    String.join(" ", Arrays.copyOfRange(args, 0, args.length)), messageContext);

        } else {
            quotes = QuoteData.getQuotes(messageContext.getGuild(), messageContext);
        }

        if (quotes.size() == 0) {
            MessageSender.sendMessage(M_NO_QUOTE_FOUND.replacement, messageContext);
            return;
        }

        Random rand = new Random();
        int i = rand.nextInt(quotes.size());

        MessageSender.sendMessage(quotes.get(i).getQuote(), messageContext);
    }
}
