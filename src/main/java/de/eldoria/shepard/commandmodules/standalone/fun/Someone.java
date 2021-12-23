package de.eldoria.shepard.commandmodules.standalone.fun;

import de.eldoria.shepard.commandmodules.Command;
import de.eldoria.shepard.commandmodules.command.CommandUsage;
import de.eldoria.shepard.commandmodules.command.Executable;
import de.eldoria.shepard.commandmodules.util.CommandCategory;
import de.eldoria.shepard.localization.util.LocalizedEmbedBuilder;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.util.Colors;
import de.eldoria.shepard.wrapper.EventContext;
import de.eldoria.shepard.wrapper.EventWrapper;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static de.eldoria.shepard.localization.enums.commands.fun.SomeoneLocale.DESCRIPTION;
import static de.eldoria.shepard.localization.enums.commands.fun.SomeoneLocale.M_BOTTLE;
import static de.eldoria.shepard.localization.enums.commands.fun.SomeoneLocale.M_NO_ONLINE;
import static de.eldoria.shepard.localization.enums.commands.fun.SomeoneLocale.M_SOMEONE;
import static de.eldoria.shepard.localization.util.TextLocalizer.localizeAllAndReplace;

/**
 * Command to tag someone in a channel who is online.
 */
@CommandUsage(EventContext.GUILD)
public class Someone extends Command implements Executable {
    /**
     * Creates a new someone command object.
     */
    public Someone() {
        super("bottlespin",
                new String[] {"bottle", "someone"},
                "command.someone.description",
                CommandCategory.FUN);
    }

    @Override
    public void execute(String label, String[] args, EventWrapper wrapper) {
        GuildChannel guildChannelById = wrapper.getGuild().get()
                .getGuildChannelById(wrapper.getMessageChannel().getId());
        if (guildChannelById != null) {
            List<Member> members = guildChannelById.getMembers().stream()
                    .filter(member -> !member.getUser().isBot())
                    .collect(Collectors.toList());

            if (members.size() == 0) {
                MessageSender.sendMessage(M_NO_ONLINE.tag, wrapper.getMessageChannel());
                return;
            }

            Random rand = new Random();

            Member member = members.get(rand.nextInt(members.size()));
            LocalizedEmbedBuilder builder = new LocalizedEmbedBuilder(wrapper)
                    .setColor(Colors.Pastel.ORANGE);
            if (label.equalsIgnoreCase("someone")) {
                builder.setDescription(localizeAllAndReplace("**" + M_SOMEONE + "**", wrapper,
                        member.getAsMention()));
            } else {
                builder.setDescription(localizeAllAndReplace("**" + M_BOTTLE + "**", wrapper,
                        member.getAsMention()));
            }

            wrapper.getMessageChannel().sendMessage(builder.build()).queue();
        }
    }
}
