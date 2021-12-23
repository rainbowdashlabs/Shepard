package de.eldoria.shepard.commandmodules.guessgame.commands;

import de.eldoria.shepard.commandmodules.Command;
import de.eldoria.shepard.commandmodules.argument.Parameter;
import de.eldoria.shepard.commandmodules.argument.SubCommand;
import de.eldoria.shepard.commandmodules.command.CommandUsage;
import de.eldoria.shepard.commandmodules.command.Executable;
import de.eldoria.shepard.commandmodules.guessgame.data.GuessGameData;
import de.eldoria.shepard.commandmodules.guessgame.util.GuessGameEvaluator;
import de.eldoria.shepard.commandmodules.guessgame.util.GuessGameImage;
import de.eldoria.shepard.commandmodules.util.CommandCategory;
import de.eldoria.shepard.database.types.Rank;
import de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale;
import de.eldoria.shepard.localization.util.LocalizedEmbedBuilder;
import de.eldoria.shepard.messagehandler.ErrorType;
import de.eldoria.shepard.messagehandler.MessageSender;
import de.eldoria.shepard.minigameutil.ChannelEvaluator;
import de.eldoria.shepard.modulebuilder.requirements.ReqDataSource;
import de.eldoria.shepard.modulebuilder.requirements.ReqInit;
import de.eldoria.shepard.modulebuilder.requirements.ReqShardManager;
import de.eldoria.shepard.util.TextFormatting;
import de.eldoria.shepard.util.reactions.ShepardEmote;
import de.eldoria.shepard.wrapper.EventContext;
import de.eldoria.shepard.wrapper.EventWrapper;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.sql.DataSource;
import java.util.List;

import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.C_SCORE;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.C_SCORE_GLOBAL;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.C_START;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.C_TOP;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.C_TOP_GLOBAL;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_GAME_DESCRIPTION;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_GAME_FOOTER;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_GLOBAL_RANKING;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_MINIGAME_CHANNEL;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_ROUND_IN_PROGRESS;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_SCORE;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_SCORE_GLOBAL;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_SERVER_RANKING;
import static de.eldoria.shepard.localization.enums.commands.fun.GuessGameLocale.M_TITLE;
import static de.eldoria.shepard.localization.util.TextLocalizer.localizeAllAndReplace;
import static de.eldoria.shepard.util.Verifier.isArgument;
import static java.lang.System.lineSeparator;

/**
 * Command to start a guess game. A started guess game will be manages by a {@link GuessGameEvaluator}. Provides
 * information about user scores.
 */
@CommandUsage( {EventContext.GUILD})
public class GuessGame extends Command implements Executable, ReqShardManager, ReqDataSource, ReqInit {

    private ChannelEvaluator<GuessGameEvaluator> evaluator;
    private ShardManager shardManager;
    private DataSource source;
    private GuessGameData guessGameData;

    /**
     * Create a new guess game command.
     */
    public GuessGame(ChannelEvaluator<GuessGameEvaluator> evaluator) {
        super("guessGame",
                new String[] {"gg", "nsfwornot"},
                "command.guessGame.description",
                SubCommand.builder("guessGame")
                        .addSubcommand("command.guessGame.subcommand.score",
                                Parameter.createCommand("score"))
                        .addSubcommand("command.guessGame.subcommand.globalScore",
                                Parameter.createCommand("scoreGlobal"))
                        .addSubcommand("command.guessGame.subcommand.top",
                                Parameter.createCommand("top"))
                        .addSubcommand("command.guessGame.subcommand.topGlobal",
                                Parameter.createCommand("topGlobal"))
                        .build(),
                C_START.tag,
                CommandCategory.FUN);
        this.evaluator = evaluator;
    }

    @Override
    public void execute(String label, String[] args, EventWrapper wrapper) {
        if (!isArgument(wrapper.getMessageChannel().getName(),
                "guessgame", "guess-game", "nsfwornot", "nsfw-or-not")) {
            MessageSender.sendMessage(M_MINIGAME_CHANNEL.tag, wrapper.getMessageChannel());
            return;
        }
        if (args.length == 0) {
            startGame(wrapper);
            return;
        }
        if (args.length != 1) {
            MessageSender.sendSimpleError(ErrorType.TOO_MANY_ARGUMENTS, wrapper);
            return;
        }

        String cmd = args[0];

        if (isSubCommand(cmd, 0)) {
            int userScore = guessGameData.getUserScore(wrapper.getGuild().get(),
                    wrapper.getAuthor(), wrapper);
            MessageSender.sendMessage(M_SCORE + " **" + userScore + "**", wrapper.getMessageChannel());
            return;
        }

        if (isSubCommand(cmd, 1)) {
            int userScore = guessGameData.getGlobalUserScore(wrapper.getAuthor(), wrapper);
            MessageSender.sendMessage(M_SCORE_GLOBAL + " **" + userScore + "**", wrapper.getMessageChannel());
            return;
        }

        if (isSubCommand(cmd, 2)) {
            sendTopScores(false, wrapper);
        }
        if (isSubCommand(cmd, 3)) {
            sendTopScores(true, wrapper);
        }
    }

    private void sendTopScores(boolean global, EventWrapper messageContext) {
        List<Rank> ranks = global
                ? guessGameData.getGlobalTopScore(10, messageContext, shardManager)
                : guessGameData.getTopScore(messageContext.getGuild().get(), 10, messageContext, shardManager);

        String rankTable = TextFormatting.getRankTable(ranks, messageContext);

        String ranking = global ? M_GLOBAL_RANKING.tag : M_SERVER_RANKING.tag;

        MessageSender.sendMessage("**" + ranking + "**" + lineSeparator() + rankTable, messageContext.getTextChannel().get());
    }

    private void startGame(EventWrapper event) {
        evaluator.scheduleEvaluation(30,
                new GuessGameEvaluator(guessGameData, evaluator, shardManager, event.getGuild().get(), event.getTextChannel().get()));
    }

    @Override
    public void addShardManager(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    @Override
    public void addDataSource(DataSource source) {
        this.source = source;
    }

    @Override
    public void init() {
        guessGameData = new GuessGameData(source);
    }
}
