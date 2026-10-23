package net.cosette.miyabi.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.util.Chat;

/**
 * Branche /miyabi progression ... : uniquement la progression (éveils, rangs et maîtrise de rang).
 * Les sorts sont dans /miyabi spell, les techniques dans /miyabi technique, les valeurs de mana et d'aura dans /miyabi stats.
 */
public final class ProgressionCommands {
    private ProgressionCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("progression")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ProgressionCommands::get)))
                // Éveils
                .then(CommandUtil.playerValue("manaawakened", "value", BoolArgumentType.bool(), ProgressionCommands::setManaAwakened))
                .then(CommandUtil.playerValue("auraawakened", "value", BoolArgumentType.bool(), ProgressionCommands::setAuraAwakened))
                .then(CommandUtil.playerValue("awakeningprogress", "value", FloatArgumentType.floatArg(0f), ProgressionCommands::setAwakeningProgress))
                // Rangs
                .then(Commands.literal("school")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(CommandUtil.enumArg("school", MagicSchool.class)
                                        .then(CommandUtil.enumArg("rank", MagicRank.class)
                                                .executes(ProgressionCommands::setSchoolRank)))))
                .then(Commands.literal("style")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(CommandUtil.enumArg("style", SwordStyle.class)
                                        .then(CommandUtil.enumArg("rank", MagicRank.class)
                                                .executes(ProgressionCommands::setStyleRank)))))
                // Maîtrise de rang (points vers le rang suivant)
                .then(Commands.literal("schoolmastery")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(CommandUtil.enumArg("school", MagicSchool.class)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ProgressionCommands::setSchoolMastery)))))
                .then(Commands.literal("stylemastery")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(CommandUtil.enumArg("style", SwordStyle.class)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ProgressionCommands::setStyleMastery)))));
    }

    private static PlayerProgressionData data(ServerPlayer player) {
        return player.getData(ModAttachments.PROGRESSION);
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        PlayerProgressionData data = data(player);

        StringBuilder schools = new StringBuilder();
        for (MagicSchool school : MagicSchool.values()) {
            if (schools.length() > 0) schools.append(", ");
            MagicRank rank = data.getSchoolRank(school);
            schools.append(Chat.school(school)).append("=").append(Chat.rank(rank));
            if (rank.next() != null) {
                schools.append(Chat.dim(" (" + data.getSchoolMastery(school) + "/" + rank.getMasteryToNext() + ")"));
            }
        }

        StringBuilder styles = new StringBuilder();
        for (SwordStyle style : SwordStyle.values()) {
            if (styles.length() > 0) styles.append(", ");
            MagicRank rank = data.getStyleRank(style);
            styles.append(Chat.style(style)).append("=").append(Chat.rank(rank));
            if (rank.next() != null) {
                styles.append(Chat.dim(" (" + data.getStyleMastery(style) + "/" + rank.getMasteryToNext() + ")"));
            }
        }

        return CommandUtil.info(ctx, Chat.title("Progression de " + player.getName().getString())
                + "\n" + Chat.label("Mana éveillé") + " : " + Chat.bool(data.isManaAwakened())
                + " | " + Chat.label("Battle aura éveillée") + " : " + Chat.bool(data.isBattleAuraAwakened())
                + " | " + Chat.label("Progression d'éveil") + " : " + Chat.mana(data.getAwakeningProgress())
                + "\n" + Chat.label("Écoles") + " : " + schools
                + "\n" + Chat.label("Styles") + " : " + styles);
    }

    private static int setSchoolRank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        MagicSchool school = CommandUtil.parseEnum(MagicSchool.class, StringArgumentType.getString(ctx, "school"));
        MagicRank rank = CommandUtil.parseEnum(MagicRank.class, StringArgumentType.getString(ctx, "rank"));
        if (school == null || rank == null) {
            return CommandUtil.fail(ctx, "École ou rang inconnu.");
        }
        data(player).setSchoolRank(school, rank);
        return CommandUtil.success(ctx, "Rang " + school.name() + " de " + player.getName().getString() + " -> " + rank.name());
    }

    private static int setStyleRank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        SwordStyle style = CommandUtil.parseEnum(SwordStyle.class, StringArgumentType.getString(ctx, "style"));
        MagicRank rank = CommandUtil.parseEnum(MagicRank.class, StringArgumentType.getString(ctx, "rank"));
        if (style == null || rank == null) {
            return CommandUtil.fail(ctx, "Style ou rang inconnu.");
        }
        data(player).setStyleRank(style, rank);
        return CommandUtil.success(ctx, "Rang " + style.name() + " de " + player.getName().getString() + " -> " + rank.name());
    }

    private static int setManaAwakened(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        boolean value = BoolArgumentType.getBool(ctx, "value");
        data(player).setManaAwakened(value);
        return CommandUtil.success(ctx, "Mana éveillé de " + player.getName().getString() + " -> " + value);
    }

    private static int setAuraAwakened(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        boolean value = BoolArgumentType.getBool(ctx, "value");
        data(player).setBattleAuraAwakened(value);
        return CommandUtil.success(ctx, "Battle aura éveillée de " + player.getName().getString() + " -> " + value);
    }

    private static int setAwakeningProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        float value = FloatArgumentType.getFloat(ctx, "value");
        data(player).setAwakeningProgress(value);
        return CommandUtil.success(ctx, "Progression d'éveil de " + player.getName().getString() + " -> " + value);
    }

    private static int setSchoolMastery(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        MagicSchool school = CommandUtil.parseEnum(MagicSchool.class, StringArgumentType.getString(ctx, "school"));
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (school == null) {
            return CommandUtil.fail(ctx, "École inconnue.");
        }
        data(player).setSchoolMastery(school, value);
        return CommandUtil.success(ctx, "Maîtrise de rang " + school.name() + " de " + player.getName().getString() + " -> " + value);
    }

    private static int setStyleMastery(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        SwordStyle style = CommandUtil.parseEnum(SwordStyle.class, StringArgumentType.getString(ctx, "style"));
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (style == null) {
            return CommandUtil.fail(ctx, "Style inconnu.");
        }
        data(player).setStyleMastery(style, value);
        return CommandUtil.success(ctx, "Maîtrise de rang " + style.name() + " de " + player.getName().getString() + " -> " + value);
    }
}