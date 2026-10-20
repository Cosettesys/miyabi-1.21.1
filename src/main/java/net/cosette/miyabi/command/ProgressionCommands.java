package net.cosette.miyabi.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.battleaura.AuraCriterion;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.util.Chat;
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
                                                .executes(ProgressionCommands::setSpellCasts)))))                .then(Commands.literal("stylemastery")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(CommandUtil.enumArg("style", SwordStyle.class)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ProgressionCommands::setStyleMastery)))))
                .then(Commands.literal("techniqueuses")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("technique", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                SwordTechniqueRegistry.all().stream().map(SwordTechniqueDefinition::id), builder))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ProgressionCommands::setTechniqueUses)))))                .then(Commands.literal("auracounter")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(CommandUtil.enumArg("criterion", AuraCriterion.class)
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                .executes(ProgressionCommands::setAuraCounter)))))                .then(Commands.literal("spelllearning")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("spell", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SpellRegistry.ids(), builder))
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(-1f, 100f))
                                                .executes(ProgressionCommands::setSpellLearning)))))
                .then(Commands.literal("learnall")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ProgressionCommands::learnAll)))
                .then(Commands.literal("spellseen")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("spell", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SpellRegistry.ids(), builder))
                                        .then(Commands.argument("seen", BoolArgumentType.bool())
                                                .executes(ProgressionCommands::setSpellSeen)))));
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
        StringBuilder casts = new StringBuilder();
        for (var entry : data.getAllSpellCasts().entrySet()) {
            if (casts.length() > 0) casts.append(", ");
            casts.append(Chat.dim(entry.getKey())).append("=").append(Chat.mana(entry.getValue()));
        }
        StringBuilder uses = new StringBuilder();
        for (var entry : data.getAllTechniqueUses().entrySet()) {
            if (uses.length() > 0) uses.append(", ");
            uses.append(Chat.dim(entry.getKey())).append("=").append(Chat.mana(entry.getValue()));
        }
        StringBuilder counters = new StringBuilder();
        for (AuraCriterion criterion : AuraCriterion.values()) {
            if (counters.length() > 0) counters.append(", ");
            counters.append(Chat.dim(criterion.name())).append("=")
                    .append(Chat.aura(String.format("%.0f", data.getAuraCounter(criterion))));
        }
        StringBuilder learning = new StringBuilder();
        for (var entry : data.getAllSpellLearning().entrySet()) {
            if (learning.length() > 0) learning.append(", ");
            learning.append(Chat.dim(entry.getKey())).append("=")
                    .append(entry.getValue() >= 100f ? Chat.good("appris") : Chat.mana(String.format("%.0f%%", entry.getValue())));
            if (entry.getValue() < 100f && data.isSpellSeenOnly(entry.getKey())) {
                learning.append(Chat.dim(" (vu)"));
            }
        }
        return CommandUtil.info(ctx, Chat.title("Progression de " + player.getName().getString())
                + "\n" + Chat.label("Mana éveillé") + " : " + Chat.bool(data.isManaAwakened())
                + " | " + Chat.label("Battle aura éveillée") + " : " + Chat.bool(data.isBattleAuraAwakened())
                + " | " + Chat.label("Niveau d'aura") + " : " + Chat.aura(data.getBattleAuraLevel())
                + " | " + Chat.label("Progression d'éveil") + " : " + Chat.mana(data.getAwakeningProgress())
                + "\n" + Chat.label("Écoles") + " : " + schools
                + "\n" + Chat.label("Styles") + " : " + styles
                + "\n" + Chat.label("Sorts lancés") + " : " + (casts.length() == 0 ? Chat.dim("aucun") : casts)
                + "\n" + Chat.label("Apprentissage") + " : " + (learning.length() == 0 ? Chat.dim("aucun sort débloqué") : learning)
                + "\n" + Chat.label("Techniques utilisées") + " : " + (uses.length() == 0 ? Chat.dim("aucune") : uses)
                + "\n" + Chat.label("Compteurs d'aura") + " : " + counters);
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
    private static int setStyleMastery(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        SwordStyle style = CommandUtil.parseEnum(SwordStyle.class, StringArgumentType.getString(ctx, "style"));
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (style == null) {
            return CommandUtil.fail(ctx, "Style inconnu.");
        }
        data(player).setStyleMastery(style, value);
        return CommandUtil.success(ctx, "Maîtrise " + style.name() + " de " + player.getName().getString() + " -> " + value);
    }
    private static int setTechniqueUses(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        String techniqueId = StringArgumentType.getString(ctx, "technique");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (SwordTechniqueRegistry.byId(techniqueId) == null) {
            return CommandUtil.fail(ctx, "Technique inconnue : " + techniqueId);
        }
        data(player).setTechniqueUses(techniqueId, value);
        return CommandUtil.success(ctx, "Utilisations de " + techniqueId + " pour " + player.getName().getString() + " -> " + value);
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
    private static int setSpellLearning(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        String spellId = StringArgumentType.getString(ctx, "spell");
        float value = FloatArgumentType.getFloat(ctx, "value");
        if (SpellRegistry.byId(spellId) == null) {
            return CommandUtil.fail(ctx, "Sort inconnu : " + spellId);
        }
        data(player).setSpellLearning(spellId, value);
        return CommandUtil.success(ctx, "Apprentissage de " + spellId + " pour " + player.getName().getString()
                + " -> " + (value < 0 ? "verrouillé" : value + "%"));
    }
    private static int learnAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        for (String id : SpellRegistry.ids()) {
            data(player).setSpellLearning(id, 100f);
        }
        return CommandUtil.success(ctx, "Tous les sorts sont appris pour " + player.getName().getString());
    }
    private static int setSpellSeen(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        String spellId = StringArgumentType.getString(ctx, "spell");
        boolean seen = BoolArgumentType.getBool(ctx, "seen");
        if (SpellRegistry.byId(spellId) == null) {
            return CommandUtil.fail(ctx, "Sort inconnu : " + spellId);
        }
        data(player).setSpellSeenOnly(spellId, seen);
        return CommandUtil.success(ctx, spellId + " de " + player.getName().getString() + " : " + (seen ? "appris en le voyant" : "appris en lisant"));
    }
}
