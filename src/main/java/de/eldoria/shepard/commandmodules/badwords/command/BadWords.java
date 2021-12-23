package de.eldoria.shepard.commandmodules.badwords.command;

import de.eldoria.shepard.basemodules.commanddispatching.util.ArgumentParser;
import de.eldoria.shepard.commandmodules.Command;
import de.eldoria.shepard.commandmodules.argument.Parameter;
import de.eldoria.shepard.commandmodules.argument.SubCommand;
import de.eldoria.shepard.commandmodules.badwords.data.BadWordData;
import de.eldoria.shepard.commandmodules.command.CommandUsage;
import de.eldoria.shepard.commandmodules.command.ExecutableAsync;
import de.eldoria.shepard.commandmodules.util.CommandCategory;
import de.eldoria.shepard.localization.enums.commands.moderation.BadWordsLocale;
import de.eldoria.shepard.localization.util.LocalizedEmbedBuilder;
import de.eldoria.shepard.localization.util.TextLocalizer;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.modulebuilder.requirements.ReqDataSource;
import de.eldoria.shepard.util.Colors;
import de.eldoria.shepard.wrapper.EventContext;
import de.eldoria.shepard.wrapper.EventWrapper;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.stream.Collectors;

@CommandUsage(EventContext.GUILD)
public class BadWords extends Command implements ExecutableAsync, ReqDataSource {

    private BadWordData commandData;

    public BadWords() {
        super("badwords",
                new String[] {"bw"},
                "command.badword.description",
                SubCommand.builder("badwords")
                        .addSubcommand("command.badword.subcommand.add", Parameter.createCommand("add"),
                                Parameter.createInput("command.general.argument.message", null, true))
                        .addSubcommand("command.badword.subcommand.remove", Parameter.createCommand("remove"),
                                Parameter.createInput("command.general.argument.message", null, true))
                        .addSubcommand("command.badword.subcommand.list", Parameter.createCommand("list"))
                        .build(),
                CommandCategory.MODERATION
        );
    }

    @Override
    public void execute(String label, String[] args, EventWrapper wrapper) {
        String cmd = args[0];

        if (isSubCommand(cmd, 0)) {
            add(args, wrapper);
        }

        if (isSubCommand(cmd, 1)) {
            remove(args, wrapper);
        }

        if (isSubCommand(cmd, 2)) {
            list(wrapper);
        }

    }

    private void list(EventWrapper wrapper) {
        String[] list = commandData.getList(wrapper.getGuild().get().getIdLong(), wrapper);
        if (list != null) {
            LocalizedEmbedBuilder leb = new LocalizedEmbedBuilder();
            leb.setTitle(BadWordsLocale.M_LIST_TITLE.tag);
            String badwords = Arrays.stream(list).map(s -> "`" + s + "`").collect(Collectors.joining(","));
            leb.setDescription(badwords);
            leb.setColor(Colors.Pastel.RED);
            wrapper.getMessageChannel().sendMessage(leb.build()).queue();

        }
    }

    private void remove(String[] args, EventWrapper wrapper) {
        String message = ArgumentParser.getMessage(args, 1);
        if (commandData.removeBadword(wrapper.getGuild().get().getIdLong(), message, wrapper)) {
            MessageSender.sendMessage(
                    TextLocalizer.localizeAllAndReplace(
                            BadWordsLocale.M_REMOVE.tag,
                            wrapper.getGuild().get(),
                            message),
                    wrapper.getMessageChannel()
            );
        }
    }

    private void add(String[] args, EventWrapper wrapper) {
        String message = ArgumentParser.getMessage(args, 1);
        if (commandData.addBadword(wrapper.getGuild().get().getIdLong(), message, wrapper)) {
            MessageSender.sendMessage(
                    TextLocalizer.localizeAllAndReplace(
                            BadWordsLocale.M_ADD.tag,
                            wrapper.getGuild().get(),
                            message),
                    wrapper.getMessageChannel()
            );
        }
    }

    @Override
    public void addDataSource(DataSource source) {
        this.commandData = new BadWordData(source);
    }

}
