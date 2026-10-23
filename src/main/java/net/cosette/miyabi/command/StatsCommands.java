package net.cosette.miyabi.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.battleaura.AuraCriterion;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.util.Chat;

/**
 * Branche /miyabi stats ... : les valeurs du mana et de la battle aura.
 * stats get PLAYER (aperçu), stats mana ... (get, set, debuglog), stats aura get|level|counter.
 * Les commandes de déblocage (mana ou aura éveillés) restent dans /miyabi progression.
 */
public final class StatsCommands {
    private StatsCommands() {}
    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("stats")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(StatsCommands::get)))
                .then(ManaCommands.branch())
                .then(Commands.literal("aura")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(StatsCommands::auraGet)))
                        .then(CommandUtil.playerValue("level", "value", IntegerArgumentType.integer(0), StatsCommands::setAuraLevel))
                        .then(Commands.literal("counter")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(CommandUtil.enumArg("criterion", AuraCriterion.class)
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                        .executes(StatsCommands::setAuraCounter))))));
    }
    private static PlayerProgressionData data(ServerPlayer player) {
        return player.getData(ModAttachments.PROGRESSION);
    }
    private static String manaLine(ServerPlayer player) {
        ManaData mana = player.getData(ModAttachments.MANA);
        return Chat.label("Mana") + " : " + Chat.mana(String.format("%.1f/%.1f", mana.getCurrent(), mana.getMax()))
                + Chat.dim(" (régénération " + mana.getRegenFlat() + " + " + mana.getRegenScaling() + "%)");
    }
    private static String auraLines(ServerPlayer player) {
        PlayerProgressionData data = data(player);
        StringBuilder counters = new StringBuilder();
        for (AuraCriterion criterion : AuraCriterion.values()) {
            if (counters.length() > 0) counters.append(", ");
            counters.append(Chat.dim(criterion.name())).append("=")
                    .append(Chat.aura(String.format("%.0f", data.getAuraCounter(criterion))));
        }
        return Chat.label("Battle aura") + " : niveau " + Chat.aura(data.getBattleAuraLevel())
                + " (" + (data.isBattleAuraAwakened() ? Chat.good("éveillée") : Chat.dim("non éveillée")) + ")"
                + "\n" + Chat.label("Compteurs d'aura") + " : " + counters;
    }
    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        return CommandUtil.info(ctx, Chat.title("Statistiques de " + player.getName().getString())
                + "\n" + manaLine(player) + "\n" + auraLines(player));
    }
    private static int auraGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        return CommandUtil.info(ctx, Chat.title("Battle aura de " + player.getName().getString()) + "\n" + auraLines(player));
    }
    private static int setAuraLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        int value = IntegerArgumentType.getInteger(ctx, "value");
        data(player).setBattleAuraLevel(value);
        return CommandUtil.success(ctx, "Niveau de battle aura de " + player.getName().getString() + " -> " + value);
    }
    private static int setAuraCounter(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        AuraCriterion criterion = CommandUtil.parseEnum(AuraCriterion.class, StringArgumentType.getString(ctx, "criterion"));
        double value = DoubleArgumentType.getDouble(ctx, "value");
        if (criterion == null) {
            return CommandUtil.fail(ctx, "Critère inconnu.");
        }
        data(player).setAuraCounter(criterion, value);
        return CommandUtil.success(ctx, "Compteur " + criterion.name() + " de " + player.getName().getString() + " -> " + value);
    }
}