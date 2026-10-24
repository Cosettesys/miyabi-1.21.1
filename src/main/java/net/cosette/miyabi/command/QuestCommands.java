package net.cosette.miyabi.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.trainer.QuestData;
import net.cosette.miyabi.trainer.QuestManager;
import net.cosette.miyabi.util.Chat;

/** Branche /miyabi quest ... : tests des quêtes d'entraîneur (get, complete, clear). */
public final class QuestCommands {
    private QuestCommands() {}
    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("quest")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player()).executes(QuestCommands::get)))
                .then(Commands.literal("complete")
                        .then(Commands.argument("player", EntityArgument.player()).executes(QuestCommands::complete)))
                .then(Commands.literal("clear")
                        .then(Commands.argument("player", EntityArgument.player()).executes(QuestCommands::clear)));
    }
    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        QuestData quest = player.getData(ModAttachments.QUEST);
        long now = player.level().getGameTime();
        StringBuilder out = new StringBuilder(Chat.title("Quête de " + player.getName().getString()));
        if (!quest.isActive()) {
            out.append("\n ").append(Chat.dim("aucune quête en cours"));
        } else {
            out.append("\n ").append(Chat.label("Type")).append(" : ").append(quest.getKind() == QuestData.Kind.LEARN
                            ? "évaluation de " + Chat.good(quest.getTechniqueId())
                            : "épreuve du rang " + Chat.rank(quest.getTargetRank()))
                    .append(Chat.dim(" (" + quest.getStyle().name() + ")"))
                    .append(" ").append(quest.isCompleted() ? Chat.good("terminée") : Chat.aura("en cours"));
            if (quest.getDeadlineTick() > 0 && !quest.isCompleted()) {
                out.append("\n ").append(Chat.label("Temps restant")).append(" : ")
                        .append(QuestManager.formatTicks(Math.max(0, quest.getDeadlineTick() - now)));
            }
            for (QuestData.Objective objective : quest.getObjectives()) {
                out.append("\n  - ").append(Chat.dim(objective.getDefinitionId())).append(" ")
                        .append(objective.getType().name()).append(" ")
                        .append(Chat.mana(objective.getProgress() + "/" + objective.getRequired()));
            }
        }
        long cooldown = quest.getCooldownUntilTick() - now;
        if (cooldown > 0) {
            out.append("\n ").append(Chat.label("Délai d'attente")).append(" : ").append(Chat.bad(QuestManager.formatTicks(cooldown)));
        }
        return CommandUtil.info(ctx, out.toString());
    }
    private static int complete(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        QuestManager.forceComplete(player);
        return CommandUtil.success(ctx, "Objectifs de " + player.getName().getString() + " remplis");
    }
    private static int clear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        QuestManager.reset(player);
        return CommandUtil.success(ctx, "Quête et délai d'attente de " + player.getName().getString() + " effacés");
    }
}