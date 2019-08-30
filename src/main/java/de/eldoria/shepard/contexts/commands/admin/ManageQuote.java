package de.eldoria.shepard.contexts.commands.admin;

import de.eldoria.shepard.contexts.commands.Command;
import de.eldoria.shepard.contexts.commands.CommandArg;
import de.eldoria.shepard.database.queries.QuoteData;
import de.eldoria.shepard.database.types.QuoteElement;
import de.eldoria.shepard.messagehandler.MessageSender;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.lineSeparator;

public class ManageQuote extends Command {

    /**
     * Create a new manage quote command object.
     */
    public ManageQuote() {
        commandName = "manageQuotes";
        commandAliases = new String[]{"mq"};
        commandDesc = "add or remove quotes";
        arguments = new CommandArg[] {
                new CommandArg("action",
                        "**addQuote** -> Adds a quote" + lineSeparator()
                                + "**removeQuote** -> Removes a Quote" + lineSeparator()
                                + "**showQuotes** -> Lists all Quotes with index",
                        true),
                new CommandArg("action",
                        "**addQuote** -> [Quote]" + lineSeparator()
                                + "**removeQuote** -> [Quote id to remove]" + lineSeparator()
                                + "**showQuotes** -> [keyword] shows all quotes which contain the keyword or"
                                + "leave empty to show all quotes",
                        false)};
    }

    @Override
    public void execute(String label, String[] args, MessageReceivedEvent receivedEvent) {
        String cmd = args[0];

        if (cmd.equalsIgnoreCase("addQuote")) {
            addQuote(args, receivedEvent);
            return;
        }

        if (cmd.equalsIgnoreCase("removeQuote")) {
            removeQuote(args, receivedEvent);
            return;
        }

        if (cmd.equalsIgnoreCase("showQuotes")) {
            showQuotes(args, receivedEvent);
            return;
        }

        MessageSender.sendSimpleError("Invalid Arguments", receivedEvent.getChannel());
    }

    private void showQuotes(String[] args, MessageReceivedEvent receivedEvent) {
        List<QuoteElement> quotes;
        if (args.length > 1) {
            quotes = QuoteData.getQuotesByKeyword(receivedEvent.getGuild(),
                    String.join(" ", Arrays.copyOfRange(args, 1, args.length)), receivedEvent);
        } else {
            quotes = QuoteData.getQuotes(receivedEvent.getGuild(), receivedEvent);
        }

        List<String> quoteStrings = new ArrayList<>();

        for (QuoteElement quote : quotes) {
            quoteStrings.add(quote.getQuoteId() + " -> " + quote.getQuote() + lineSeparator());
        }

        List<String> messageFragments = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (String quote : quoteStrings) {
            if (builder.length() + quote.length() > 2000) {
                messageFragments.add(builder.toString());
                builder.setLength(0);
            }
            builder.append(quote);
        }
        messageFragments.add(builder.toString());


        for (String message : messageFragments) {
            MessageSender.sendMessage(message, receivedEvent.getChannel());
        }
    }

    private void removeQuote(String[] args, MessageReceivedEvent receivedEvent) {
        if (args.length != 2) {
            MessageSender.sendSimpleError("Invalid Argument", receivedEvent.getChannel());
        }

        int quotesCount = QuoteData.getQuotesCount(receivedEvent.getGuild(), receivedEvent);
        int quoteId;
        try {
            quoteId = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException e) {
            MessageSender.sendSimpleError("This is not a number", receivedEvent.getChannel());
            return;
        }

        if (quoteId > quotesCount) {
            MessageSender.sendSimpleError("There is no quote with this id!", receivedEvent.getChannel());
        }

        QuoteData.removeQuote(receivedEvent.getGuild(), quoteId, receivedEvent);
        MessageSender.sendMessage("Remove quote with id **" + quoteId + "**", receivedEvent.getChannel());
    }

    private void addQuote(String[] args, MessageReceivedEvent receivedEvent) {
        if (args.length == 1) {
            MessageSender.sendSimpleError("No Quote found!", receivedEvent.getChannel());
            return;
        }

        String quote = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        QuoteData.addQuote(receivedEvent.getGuild(), quote, receivedEvent);
        MessageSender.sendSimpleTextBox("Saved Quote!", quote, Color.blue, receivedEvent.getChannel());
    }
}
