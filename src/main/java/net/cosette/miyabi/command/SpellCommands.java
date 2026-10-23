package net.cosette.miyabi.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.util.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Branche /miyabi spell ... :
 * get, give (avec seen en option), setlearning, remove, mastery get|set. "all" désigne tous les sorts (give et remove).
 */
public final class SpellCommands {
    private SpellCommands() {}
    private static final String ALL = "all";
    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("spell")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(SpellCommands::get)))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(spellArg(true)
                                        .executes(ctx -> give(ctx, false))
                                        .then(Commands.literal("seen").executes(ctx -> give(ctx, true))))))
                .then(Commands.literal("setlearning")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(spellArg(false)
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 100f))
                                                .executes(SpellCommands::setLearning)))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(spellArg(true).executes(SpellCommands::remove))))
                .then(Commands.literal("mastery")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(spellArg(false).executes(SpellCommands::masteryGet))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(spellArg(false)
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(SpellCommands::masterySet))))));
    }
    private static RequiredArgumentBuilder<CommandSourceStack, String> spellArg(boolean withAll) {
        return Commands.argument("spell", StringArgumentType.word())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        withAll ? Stream.concat(Stream.of(ALL), SpellRegistry.ids().stream()) : SpellRegistry.ids().stream(), builder));
    }
    /** Le sort désigné (ou tous), ou null si l'id est inconnu. */
    private static List<SpellDefinition> resolve(CommandContext<CommandSourceStack> ctx, boolean allowAll) {
        String id = StringArgumentType.getString(ctx, "spell");
        List<SpellDefinition> result = new ArrayList<>();
        if (allowAll && ALL.equalsIgnoreCase(id)) {
            for (String spellId : SpellRegistry.ids()) {
                result.add(SpellRegistry.byId(spellId));
            }
            return result;
        }
        SpellDefinition def = SpellRegistry.byId(id);
        if (def == null) return null;
        result.add(def);
        return result;
    }
    private static PlayerProgressionData data(ServerPlayer player) {
        return player.getData(ModAttachments.PROGRESSION);
    }
    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        PlayerProgressionData data = data(player);
        StringBuilder out = new StringBuilder(Chat.title("Sorts de " + player.getName().getString()));
        int count = 0;
        for (String id : SpellRegistry.ids()) {
            if (!data.isSpellUnlocked(id)) continue;
            SpellDefinition def = SpellRegistry.byId(id);
            count++;
            out.append("\n ").append(Chat.schools(def.schools())).append(" ").append(id)
                    .append(Chat.dim(" [" + def.rank().name() + "] "));
            if (data.isSpellLearned(id)) {
                out.append(Chat.good("appris"));
            } else {
                out.append(Chat.mana(String.format("%.0f%%", data.getSpellLearning(id))));
                if (data.isSpellSeenOnly(id)) out.append(Chat.dim(" (vu)"));
            }
            out.append(Chat.dim("  maîtrise " + data.getSpellCasts(id) + "/" + def.masteryCastsRequired()));
        }
        if (count == 0) out.append("\n ").append(Chat.dim("aucun sort débloqué"));
        return CommandUtil.info(ctx, out.toString());
    }
    private static int give(CommandContext<CommandSourceStack> ctx, boolean seen) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SpellDefinition> spells = resolve(ctx, true);
        if (spells == null) return CommandUtil.fail(ctx, "Sort inconnu : " + StringArgumentType.getString(ctx, "spell"));
        for (SpellDefinition def : spells) {
            data(player).unlockSpell(def.id());
            data(player).setSpellSeenOnly(def.id(), seen);
        }
        return CommandUtil.success(ctx, spells.size() + " sort(s) débloqué(s) pour " + player.getName().getString()
                + (seen ? " (vus)" : " (formule connue)"));
    }
    private static int setLearning(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SpellDefinition> spells = resolve(ctx, false);
        if (spells == null) return CommandUtil.fail(ctx, "Sort inconnu : " + StringArgumentType.getString(ctx, "spell"));
        float value = FloatArgumentType.getFloat(ctx, "value");
        data(player).setSpellLearning(spells.get(0).id(), value);
        return CommandUtil.success(ctx, "Apprentissage de " + spells.get(0).id() + " pour " + player.getName().getString() + " -> " + value + "%");
    }
    /** Verrouille le sort et remet à zéro son apprentissage, son marquage "vu" et sa maîtrise. */
    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SpellDefinition> spells = resolve(ctx, true);
        if (spells == null) return CommandUtil.fail(ctx, "Sort inconnu : " + StringArgumentType.getString(ctx, "spell"));
        for (SpellDefinition def : spells) {
            data(player).setSpellLearning(def.id(), -1f);
            data(player).setSpellSeenOnly(def.id(), false);
            data(player).setSpellCasts(def.id(), 0);
        }
        return CommandUtil.success(ctx, spells.size() + " sort(s) retiré(s) à " + player.getName().getString());
    }
    private static int masteryGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SpellDefinition> spells = resolve(ctx, false);
        if (spells == null) return CommandUtil.fail(ctx, "Sort inconnu : " + StringArgumentType.getString(ctx, "spell"));
        SpellDefinition def = spells.get(0);
        int casts = data(player).getSpellCasts(def.id());
        int required = def.masteryCastsRequired();
        return CommandUtil.info(ctx, Chat.label("Maîtrise de " + def.id()) + " pour " + player.getName().getString() + " : "
                + Chat.mana(casts + "/" + required) + Chat.dim(String.format(" (%.0f%%)", Math.min(100f, casts * 100f / required))));
    }
    private static int masterySet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SpellDefinition> spells = resolve(ctx, false);
        if (spells == null) return CommandUtil.fail(ctx, "Sort inconnu : " + StringArgumentType.getString(ctx, "spell"));
        int value = IntegerArgumentType.getInteger(ctx, "value");
        SpellDefinition def = spells.get(0);
        data(player).setSpellCasts(def.id(), value);
        return CommandUtil.success(ctx, "Maîtrise de " + def.id() + " pour " + player.getName().getString()
                + " -> " + value + "/" + def.masteryCastsRequired());
    }
}