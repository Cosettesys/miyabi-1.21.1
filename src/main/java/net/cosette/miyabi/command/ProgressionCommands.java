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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.skills.magic.MagicSchool;

/** Branche /miyabi progression ... */
public final class ProgressionCommands {
    private ProgressionCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("progression")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ProgressionCommands::get)))
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
                .then(CommandUtil.playerValue("manaawakened", "value", BoolArgumentType.bool(), ProgressionCommands::setManaAwakened))
                .then(CommandUtil.playerValue("auraawakened", "value", BoolArgumentType.bool(), ProgressionCommands::setAuraAwakened))
                .then(CommandUtil.playerValue("auralevel", "value", IntegerArgumentType.integer(0), ProgressionCommands::setAuraLevel))
                .then(CommandUtil.playerValue("awakeningprogress", "value", FloatArgumentType.floatArg(0f), ProgressionCommands::setAwakeningProgress))
                .then(Commands.literal("mastery")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(CommandUtil.enumArg("school", MagicSchool.class)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ProgressionCommands::setMastery)))))
                .then(Commands.literal("spellcasts")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("spell", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SpellRegistry.ids(), builder))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ProgressionCommands::setSpellCasts)))));
    }

    private static PlayerProgressionData data(ServerPlayer player) {
        return player.getData(PlayerProgressionData.PROGRESSION);
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        PlayerProgressionData data = data(player);

        StringBuilder schools = new StringBuilder();
        for (MagicSchool school : MagicSchool.values()) {
            if (schools.length() > 0) schools.append(", ");
            MagicRank rank = data.getSchoolRank(school);
            schools.append(school.name()).append("=").append(rank.name());
            if (rank.next() != null) {
                schools.append(" (").append(data.getSchoolMastery(school))
                        .append("/").append(rank.getMasteryToNext()).append(")");
            }
        }

        StringBuilder styles = new StringBuilder();
        for (SwordStyle style : SwordStyle.values()) {
            if (styles.length() > 0) styles.append(", ");
            styles.append(style.name()).append("=").append(data.getStyleRank(style).name());
        }

        StringBuilder casts = new StringBuilder();
        for (var entry : data.getAllSpellCasts().entrySet()) {
            if (casts.length() > 0) casts.append(", ");
            casts.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return CommandUtil.info(ctx, "Progression de " + player.getName().getString()
                + "\nMana éveillé : " + data.isManaAwakened()
                + " | Battle aura éveillée : " + data.isBattleAuraAwakened()
                + " | Niveau d'aura : " + data.getBattleAuraLevel()
                + " | Progression d'éveil : " + data.getAwakeningProgress()
                + "\nÉcoles : " + schools
                + "\nStyles : " + styles
                + "\nSorts lancés : " + (casts.length() == 0 ? "aucun" : casts));
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

    private static int setAuraLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        int value = IntegerArgumentType.getInteger(ctx, "value");
        data(player).setBattleAuraLevel(value);
        return CommandUtil.success(ctx, "Niveau de battle aura de " + player.getName().getString() + " -> " + value);
    }

    private static int setAwakeningProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        float value = FloatArgumentType.getFloat(ctx, "value");
        data(player).setAwakeningProgress(value);
        return CommandUtil.success(ctx, "Progression d'éveil de " + player.getName().getString() + " -> " + value);
    }

    private static int setMastery(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        MagicSchool school = CommandUtil.parseEnum(MagicSchool.class, StringArgumentType.getString(ctx, "school"));
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (school == null) {
            return CommandUtil.fail(ctx, "École inconnue.");
        }
        data(player).setSchoolMastery(school, value);
        return CommandUtil.success(ctx, "Maîtrise " + school.name() + " de " + player.getName().getString() + " -> " + value);
    }

    private static int setSpellCasts(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        String spellId = StringArgumentType.getString(ctx, "spell");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (SpellRegistry.byId(spellId) == null) {
            return CommandUtil.fail(ctx, "Sort inconnu : " + spellId);
        }
        data(player).setSpellCasts(spellId, value);
        return CommandUtil.success(ctx, "Lancers de " + spellId + " pour " + player.getName().getString() + " -> " + value);
    }
}
