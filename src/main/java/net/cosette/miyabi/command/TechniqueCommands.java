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
import net.cosette.miyabi.progression.MasteryManager;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.util.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Branche /miyabi technique ... : mêmes commandes que /miyabi spell, pour les techniques d'épée. */
public final class TechniqueCommands {
    private TechniqueCommands() {}
    private static final String ALL = "all";
    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("technique")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(TechniqueCommands::get)))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(techniqueArg(true)
                                        .executes(ctx -> give(ctx, false))
                                        .then(Commands.literal("seen").executes(ctx -> give(ctx, true))))))
                .then(Commands.literal("setlearning")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(techniqueArg(false)
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 100f))
                                                .executes(TechniqueCommands::setLearning)))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(techniqueArg(true).executes(TechniqueCommands::remove))))
                .then(Commands.literal("mastery")
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(techniqueArg(false).executes(TechniqueCommands::masteryGet))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(techniqueArg(false)
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(TechniqueCommands::masterySet))))));
    }
    private static RequiredArgumentBuilder<CommandSourceStack, String> techniqueArg(boolean withAll) {
        return Commands.argument("technique", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Stream<String> ids = SwordTechniqueRegistry.all().stream().map(SwordTechniqueDefinition::id);
                    return SharedSuggestionProvider.suggest(withAll ? Stream.concat(Stream.of(ALL), ids) : ids, builder);
                });
    }
    private static List<SwordTechniqueDefinition> resolve(CommandContext<CommandSourceStack> ctx, boolean allowAll) {
        String id = StringArgumentType.getString(ctx, "technique");
        if (allowAll && ALL.equalsIgnoreCase(id)) {
            return new ArrayList<>(SwordTechniqueRegistry.all());
        }
        SwordTechniqueDefinition def = SwordTechniqueRegistry.byId(id);
        if (def == null) return null;
        List<SwordTechniqueDefinition> result = new ArrayList<>();
        result.add(def);
        return result;
    }
    private static PlayerProgressionData data(ServerPlayer player) {
        return player.getData(ModAttachments.PROGRESSION);
    }
    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        PlayerProgressionData data = data(player);
        StringBuilder out = new StringBuilder(Chat.title("Techniques de " + player.getName().getString()));
        int count = 0;
        for (SwordTechniqueDefinition def : SwordTechniqueRegistry.all()) {
            if (!data.isTechniqueUnlocked(def.id())) continue;
            count++;
            out.append("\n ").append(Chat.style(def.style())).append(" ").append(def.id())
                    .append(Chat.dim(" [" + def.rank().name() + "] "));
            if (data.isTechniqueLearned(def.id())) {
                out.append(Chat.good("apprise"));
            } else {
                out.append(Chat.mana(String.format("%.0f%%", data.getTechniqueLearning(def.id()))));
                if (data.isTechniqueSeenOnly(def.id())) out.append(Chat.dim(" (vue)"));
            }
            out.append(Chat.dim("  maîtrise " + data.getTechniqueUses(def.id()) + "/" + MasteryManager.TECHNIQUE_MASTERY_USES));
        }
        if (count == 0) out.append("\n ").append(Chat.dim("aucune technique débloquée"));
        return CommandUtil.info(ctx, out.toString());
    }
    private static int give(CommandContext<CommandSourceStack> ctx, boolean seen) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SwordTechniqueDefinition> techniques = resolve(ctx, true);
        if (techniques == null) return CommandUtil.fail(ctx, "Technique inconnue : " + StringArgumentType.getString(ctx, "technique"));
        for (SwordTechniqueDefinition def : techniques) {
            data(player).unlockTechnique(def.id());
            data(player).setTechniqueSeenOnly(def.id(), seen);
        }
        return CommandUtil.success(ctx, techniques.size() + " technique(s) débloquée(s) pour " + player.getName().getString()
                + (seen ? " (vues)" : " (formation reçue)"));
    }
    private static int setLearning(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SwordTechniqueDefinition> techniques = resolve(ctx, false);
        if (techniques == null) return CommandUtil.fail(ctx, "Technique inconnue : " + StringArgumentType.getString(ctx, "technique"));
        float value = FloatArgumentType.getFloat(ctx, "value");
        data(player).setTechniqueLearning(techniques.get(0).id(), value);
        return CommandUtil.success(ctx, "Apprentissage de " + techniques.get(0).id() + " pour " + player.getName().getString() + " -> " + value + "%");
    }
    /** Verrouille la technique et remet à zéro son apprentissage, son marquage "vue" et sa maîtrise. */
    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SwordTechniqueDefinition> techniques = resolve(ctx, true);
        if (techniques == null) return CommandUtil.fail(ctx, "Technique inconnue : " + StringArgumentType.getString(ctx, "technique"));
        for (SwordTechniqueDefinition def : techniques) {
            data(player).setTechniqueLearning(def.id(), -1f);
            data(player).setTechniqueSeenOnly(def.id(), false);
            data(player).setTechniqueUses(def.id(), 0);
        }
        return CommandUtil.success(ctx, techniques.size() + " technique(s) retirée(s) à " + player.getName().getString());
    }
    private static int masteryGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SwordTechniqueDefinition> techniques = resolve(ctx, false);
        if (techniques == null) return CommandUtil.fail(ctx, "Technique inconnue : " + StringArgumentType.getString(ctx, "technique"));
        SwordTechniqueDefinition def = techniques.get(0);
        int uses = data(player).getTechniqueUses(def.id());
        int required = MasteryManager.TECHNIQUE_MASTERY_USES;
        return CommandUtil.info(ctx, Chat.label("Maîtrise de " + def.id()) + " pour " + player.getName().getString() + " : "
                + Chat.mana(uses + "/" + required) + Chat.dim(String.format(" (%.0f%%)", Math.min(100f, uses * 100f / required))));
    }
    private static int masterySet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        List<SwordTechniqueDefinition> techniques = resolve(ctx, false);
        if (techniques == null) return CommandUtil.fail(ctx, "Technique inconnue : " + StringArgumentType.getString(ctx, "technique"));
        int value = IntegerArgumentType.getInteger(ctx, "value");
        SwordTechniqueDefinition def = techniques.get(0);
        data(player).setTechniqueUses(def.id(), value);
        return CommandUtil.success(ctx, "Maîtrise de " + def.id() + " pour " + player.getName().getString()
                + " -> " + value + "/" + MasteryManager.TECHNIQUE_MASTERY_USES);
    }
}