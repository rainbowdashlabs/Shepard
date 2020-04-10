package de.eldoria.shepard.listener;

import de.eldoria.shepard.contexts.commands.ArgumentParser;
import de.eldoria.shepard.database.queries.commands.GreetingData;
import de.eldoria.shepard.database.queries.commands.InviteData;
import de.eldoria.shepard.database.types.DatabaseInvite;
import de.eldoria.shepard.database.types.GreetingSettings;
import de.eldoria.shepard.messagehandler.MessageSender;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GreetingListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        CompletableFuture.runAsync(() -> handleGreeting(event));
    }

    private void handleGreeting(GuildMemberJoinEvent event) {
        GreetingSettings greeting = GreetingData.getGreeting(event.getGuild());

        if (greeting == null) return;

        TextChannel textChannel = ArgumentParser.getTextChannel(event.getGuild(), greeting.getChannel().getId());

        if (textChannel == null) return;

        List<Invite> serverInvites = event.getGuild().retrieveInvites().complete();

        List<DatabaseInvite> databaseInvites = InviteData.getInvites(event.getGuild(), null);

        for (Invite sInvite : serverInvites) {
            for (DatabaseInvite dInvite : databaseInvites) {
                if (sInvite.getUses() != dInvite.getUsedCount()) {
                    for (int i = dInvite.getUsedCount(); i < sInvite.getUses(); i++) {
                        InviteData.upCountInvite(event.getGuild(), sInvite.getCode(), null);
                    }
                    MessageSender.sendGreeting(event, greeting, dInvite.getSource(), textChannel);
                    return;
                }
            }
        }

        //If no invite was found.
        MessageSender.sendGreeting(event, greeting, null, textChannel);
    }
}
