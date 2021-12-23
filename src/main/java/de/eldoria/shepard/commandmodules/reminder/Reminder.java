package de.eldoria.shepard.commandmodules.reminder;

import de.eldoria.shepard.basemodules.commanddispatching.util.ArgumentParser;
import de.eldoria.shepard.commandmodules.Command;
import de.eldoria.shepard.commandmodules.argument.Parameter;
import de.eldoria.shepard.commandmodules.argument.SubCommand;
import de.eldoria.shepard.commandmodules.command.Executable;
import de.eldoria.shepard.commandmodules.reminder.data.ReminderData;
import de.eldoria.shepard.commandmodules.reminder.types.ReminderComplex;
import de.eldoria.shepard.commandmodules.reminder.types.ReminderSimple;
import de.eldoria.shepard.commandmodules.util.CommandCategory;
import de.eldoria.shepard.localization.util.LocalizedEmbedBuilder;
import de.eldoria.shepard.localization.util.TextLocalizer;
import de.eldoria.shepard.messagehandler.ErrorType;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.modulebuilder.requirements.ReqDataSource;
import de.eldoria.shepard.util.Colors;
import de.eldoria.shepard.util.TextFormatting;
import de.eldoria.shepard.wrapper.EventWrapper;
import net.dv8tion.jda.api.entities.PrivateChannel;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Pattern;

import static de.eldoria.shepard.localization.enums.WordsLocale.*;
import static de.eldoria.shepard.localization.enums.commands.util.ReminderLocal.*;
import static de.eldoria.shepard.localization.util.TextLocalizer.localizeAllAndReplace;

/**
 * Reminder command which reminds user on a specific time and date or at a interval.
 */
public class Reminder extends Command implements Executable, ReqDataSource {

    private static final Pattern DATE = Pattern.compile("[0-9]{1,2}\\.[0-9]{1,2}\\.\\s[0-9]{1,2}:[0-9]{1,2}",
            Pattern.MULTILINE);

    private ReminderData reminderData;

    /**
     * Creates a new reminder command object.
     */
    public Reminder() {
        super("remind",
                new String[]{"reminder", "timer"},
                "command.reminder.description",
                SubCommand.builder("remind")
                        .addSubcommand("command.reminder.subcommand.add",
                                Parameter.createInput("command.reminder.argument.timestamp", "command.reminder.argumentDescription.timestamp", true),
                                Parameter.createInput("command.general.argument.message", null, true))
                        .addSubcommand("command.reminder.subcommand.remove",
                                Parameter.createCommand("delete"),
                                Parameter.createInput("command.general.argument.id", "command.general.argumentDescription.id", true))
                        .addSubcommand("command.reminder.subcommand.snooze",
                                Parameter.createCommand("snooze"),
                                Parameter.createInput("command.general.argument.id", "command.general.argumentDescription.id", true),
                                Parameter.createInput("command.reminder.argument.interval", "command.reminder.argumentDescription.interval", true))
                        .addSubcommand("command.reminder.subcommand.restore",
                                Parameter.createCommand("restore"),
                                Parameter.createInput("command.general.argument.id", "command.general.argumentDescription.id", true))
                        .addSubcommand("command.reminder.subcommand.list",
                                Parameter.createCommand("list"))
                        .build(),
                CommandCategory.UTIL);
    }

    @Override
    public void execute(String label, String[] args, EventWrapper wrapper) {
        String cmd = args[0];

        if (isSubCommand(cmd, 4)) {
            list(wrapper);
            return;
        }

        if (args.length < 2) {
            MessageSender.sendSimpleError(ErrorType.TOO_FEW_ARGUMENTS, wrapper);
            return;
        }

        if (isSubCommand(cmd, 1)) {
            delete(args, wrapper);
            return;
        }

        if (isSubCommand(cmd, 2)) {
            snooze(args, wrapper);
            return;
        }

        if (isSubCommand(cmd, 3)) {
            restore(args[1], wrapper);
            return;
        }


        if (args.length < 3) {
            MessageSender.sendSimpleError(ErrorType.TOO_FEW_ARGUMENTS, wrapper);
            return;
        }

        add(args, wrapper);
    }

    private void restore(String id, EventWrapper wrapper) {
        OptionalLong number = ArgumentParser.hexToLong(id);
        if (number.isEmpty()) {
            MessageSender.sendSimpleError(ErrorType.INVALID_ID, wrapper);
            return;
        }

        Optional<ReminderComplex> optionalReminder = reminderData.getReminder(number.getAsLong(), wrapper);
        if (optionalReminder.isEmpty()) {
            MessageSender.sendSimpleError(ErrorType.INVALID_ID, wrapper);
            return;
        }

        ReminderComplex reminder = optionalReminder.get();

        if (reminderData.restoreReminder(number.getAsLong(), wrapper)) {
            LocalizedEmbedBuilder builder = new LocalizedEmbedBuilder(wrapper)
                    .setTitle(localizeAllAndReplace(M_TITLE_RESTORED.tag, wrapper, reminder.getReminderId()))
                    .setDescription(localizeAllAndReplace("**" + M_REMIND_DISPLAY.tag + "**",
                            wrapper, reminder.getTimeString()) + System.lineSeparator() + reminder.getText())
                    .setColor(Colors.Pastel.ORANGE);
            wrapper.getMessageChannel().sendMessage(builder.build()).queue();
        }
    }

    private void snooze(String[] args, EventWrapper wrapper) {
        OptionalLong number = ArgumentParser.hexToLong(args[1]);
        if (number.isEmpty()) {
            MessageSender.sendSimpleError(ErrorType.INVALID_ID, wrapper);
            return;
        }

        Optional<ReminderComplex> optionalReminder = reminderData.getReminder(number.getAsLong(), wrapper);
        if (optionalReminder.isEmpty()) {
            MessageSender.sendSimpleError(ErrorType.INVALID_ID, wrapper);
            return;
        }

        String timeString = ArgumentParser.getMessage(args, -2);
        if (!ArgumentParser.getInterval(timeString)) {
            MessageSender.sendSimpleError(ErrorType.INVALID_TIME, wrapper);
            return;
        }

        reminderData.snoozeReminder(number.getAsLong(), timeString, wrapper);

        ReminderComplex reminder = reminderData.getReminder(number.getAsLong(), wrapper).get();

        LocalizedEmbedBuilder builder = new LocalizedEmbedBuilder(wrapper)
                .setTitle(localizeAllAndReplace(M_TITLE_SNOOZED.tag, wrapper, reminder.getReminderId()))
                .setDescription(localizeAllAndReplace("**" + M_REMIND_TIME.tag + "**",
                        wrapper, timeString) + System.lineSeparator() + reminder.getText())
                .setColor(Colors.Pastel.BLUE);
        wrapper.getMessageChannel().sendMessage(builder.build()).queue();
    }

    private void delete(String[] args, EventWrapper wrapper) {
        OptionalLong number = ArgumentParser.hexToLong(args[1]);
        if (number.isEmpty()) {
            MessageSender.sendSimpleError(ErrorType.INVALID_ID, wrapper);
            return;
        }

        Optional<ReminderComplex> optionalReminder = reminderData.getReminder(number.getAsLong(), wrapper);
        if (optionalReminder.isEmpty()) {
            MessageSender.sendSimpleError(ErrorType.INVALID_ID, wrapper);
            return;
        }

        ReminderSimple reminder = optionalReminder.get();

        if (reminderData.removeUserReminder(number.getAsLong(), wrapper)) {
            LocalizedEmbedBuilder builder = new LocalizedEmbedBuilder(wrapper)
                    .setTitle(localizeAllAndReplace(M_TITLE_REMOVED.tag, wrapper, reminder.getReminderId()))
                    .setDescription(TextFormatting.cropText(reminder.getText(), "...", 240, true))
                    .setFooter(localizeAllAndReplace(M_REMOVED.tag, wrapper, reminder.getTimeString()))
                    .setColor(Colors.Pastel.RED);
            wrapper.getMessageChannel().sendMessage(builder.build()).queue();
        } else {
            MessageSender.sendSimpleError(ErrorType.INVALID_ID, wrapper);
        }
    }

    private void list(EventWrapper messageContext) {
        List<ReminderSimple> reminders = reminderData.getUserReminder(messageContext);

        TextFormatting.TableBuilder tableBuilder = TextFormatting.getTableBuilder(
                reminders,
                TextLocalizer.localizeAll(ID.tag, messageContext),
                TextLocalizer.localizeAll(MESSAGE.tag, messageContext),
                TextLocalizer.localizeAll(TIME.tag, messageContext));
        for (ReminderSimple reminder : reminders) {
            tableBuilder.next();
            tableBuilder.setRow(
                    reminder.getReminderId() + "",
                    TextFormatting.cropText(reminder.getText(), "...", 30, true),
                    reminder.getTimeString());
        }

        MessageSender.sendMessage(M_CURRENT_REMINDERS + System.lineSeparator() + tableBuilder,
                messageContext.getMessageChannel());
    }

    private void add(String[] args, EventWrapper wrapper) {
        String timeString = ArgumentParser.getMessage(args, 0, 2);
        if (!DATE.matcher(timeString).find() && !ArgumentParser.getInterval(timeString)) {
            MessageSender.sendSimpleError(ErrorType.INVALID_TIME, wrapper);
            return;
        }

        String message = ArgumentParser.getMessage(args, 2, 0);
        // Date reminder
        if (DATE.matcher(timeString).find()) {
            String date = args[0];
            String time = args[1];

            Optional<String> id = reminderData.addReminderDate(message, date, time, wrapper);
            if (id.isPresent()) {
                LocalizedEmbedBuilder builder = new LocalizedEmbedBuilder(wrapper)
                        .setTitle(localizeAllAndReplace(M_TITLE_CREATED.tag, wrapper, id.get()))
                        .setDescription(localizeAllAndReplace("**" + M_REMIND_DATE.tag + "**",
                                wrapper, date, time) + System.lineSeparator() + message);
                if (wrapper.isPrivateEvent()) {
                    Optional<PrivateChannel> privateChannel = wrapper.getPrivateChannel();
                    privateChannel.ifPresent(channel -> channel.sendMessage(builder.build()).queue());
                    return;
                }
                wrapper.getMessageChannel().sendMessage(builder.build()).queue();
            }
            return;
        }

        // Interval reminder
        Optional<String> id = reminderData.addReminderInterval(message, timeString, wrapper);
        if (id.isPresent()) {
            LocalizedEmbedBuilder builder = new LocalizedEmbedBuilder(wrapper)
                    .setTitle(localizeAllAndReplace(M_TITLE_CREATED.tag, wrapper, id.get()))
                    .setDescription(localizeAllAndReplace("**" + M_REMIND_TIME.tag + "**",
                            wrapper, timeString) + System.lineSeparator() + message);
            if (wrapper.isPrivateEvent()) {
                Optional<PrivateChannel> privateChannel = wrapper.getPrivateChannel();
                privateChannel.ifPresent(channel -> channel.sendMessage(builder.build()).queue());
                return;
            }
            wrapper.getMessageChannel().sendMessage(builder.build()).queue();
        }
    }

    @Override
    public void addDataSource(DataSource source) {
        reminderData = new ReminderData(source);
    }
}
