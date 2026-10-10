package net.cosette.miyabi.progression;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cosette.miyabi.skills.SpellRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.magic.spell.MagicSchool;

import java.util.Arrays;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ProgressionCommands {
    private ProgressionCommands() {}
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }
    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("progression")
                .requires(src -> src.hasPermission(2));
        root.then(Commands.literal("get")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ProgressionCommands::get)));
        root.then(Commands.literal("school")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("school", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(names(MagicSchool.values()), builder))
                                .then(Commands.argument("rank", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(names(MagicRank.values()), builder))
                                        .executes(ProgressionCommands::setSchoolRank)))));
        root.then(Commands.literal("style")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("style", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(names(SwordStyle.values()), builder))
                                .then(Commands.argument("rank", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(names(MagicRank.values()), builder))
                                        .executes(ProgressionCommands::setStyleRank)))));
        root.then(Commands.literal("manaawakened")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ProgressionCommands::setManaAwakened))));
        root.then(Commands.literal("auraawakened")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ProgressionCommands::setAuraAwakened))));
        root.then(Commands.literal("auralevel")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(ProgressionCommands::setAuraLevel))));
        root.then(Commands.literal("awakeningprogress")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0f))
                                .executes(ProgressionCommands::setAwakeningProgress))));
        root.then(Commands.literal("mastery")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("school", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(names(MagicSchool.values()), builder))
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(ProgressionCommands::setMastery)))));

        root.then(Commands.literal("spellcasts")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("spell", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SpellRegistry.ids(), builder))
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(ProgressionCommands::setSpellCasts)))));
        dispatcher.register(Commands.literal("miyabi").then(root));
    }
    private static <E extends Enum<E>> Stream<String> names(E[] values) {
        return Arrays.stream(values).map(Enum::name);
    }
    private static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        try {
            return Enum.valueOf(type, raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        PlayerProgressionData data = player.getData(PlayerProgressionData.PROGRESSION);
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
        String message = "Progression de " + player.getName().getString()
                + "\nMana éveillé : " + data.isManaAwakened()
                + " | Battle aura éveillée : " + data.isBattleAuraAwakened()
                + " | Niveau d'aura : " + data.getBattleAuraLevel()
                + " | Progression d'éveil : " + data.getAwakeningProgress()
                + "\nÉcoles : " + schools
                + "\nStyles : " + styles
                + "\nSorts lancés : " + (casts.length() == 0 ? "aucun" : casts);
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }
    private static int setSchoolRank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        MagicSchool school = parseEnum(MagicSchool.class, StringArgumentType.getString(ctx, "school"));
        MagicRank rank = parseEnum(MagicRank.class, StringArgumentType.getString(ctx, "rank"));
        if (school == null || rank == null) {
            ctx.getSource().sendFailure(Component.literal("École ou rang inconnu."));
            return 0;
        }
        player.getData(PlayerProgressionData.PROGRESSION).setSchoolRank(school, rank);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Rang " + school.name() + " de " + player.getName().getString() + " -> " + rank.name()), true);
        return 1;
    }
    private static int setStyleRank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        SwordStyle style = parseEnum(SwordStyle.class, StringArgumentType.getString(ctx, "style"));
        MagicRank rank = parseEnum(MagicRank.class, StringArgumentType.getString(ctx, "rank"));
        if (style == null || rank == null) {
            ctx.getSource().sendFailure(Component.literal("Style ou rang inconnu."));
            return 0;
        }
        player.getData(PlayerProgressionData.PROGRESSION).setStyleRank(style, rank);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Rang " + style.name() + " de " + player.getName().getString() + " -> " + rank.name()), true);
        return 1;
    }
    private static int setManaAwakened(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        boolean value = BoolArgumentType.getBool(ctx, "value");
        player.getData(PlayerProgressionData.PROGRESSION).setManaAwakened(value);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Mana éveillé de " + player.getName().getString() + " -> " + value), true);
        return 1;
    }
    private static int setAuraAwakened(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        boolean value = BoolArgumentType.getBool(ctx, "value");
        player.getData(PlayerProgressionData.PROGRESSION).setBattleAuraAwakened(value);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Battle aura éveillée de " + player.getName().getString() + " -> " + value), true);
        return 1;
    }
    private static int setAuraLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        player.getData(PlayerProgressionData.PROGRESSION).setBattleAuraLevel(value);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Niveau de battle aura de " + player.getName().getString() + " -> " + value), true);
        return 1;
    }
    private static int setAwakeningProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        float value = FloatArgumentType.getFloat(ctx, "value");
        player.getData(PlayerProgressionData.PROGRESSION).setAwakeningProgress(value);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Progression d'éveil de " + player.getName().getString() + " -> " + value), true);
        return 1;
    }
    private static int setMastery(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        MagicSchool school = parseEnum(MagicSchool.class, StringArgumentType.getString(ctx, "school"));
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (school == null) {
            ctx.getSource().sendFailure(Component.literal("École inconnue."));
            return 0;
        }
        player.getData(PlayerProgressionData.PROGRESSION).setSchoolMastery(school, value);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Maîtrise " + school.name() + " de " + player.getName().getString() + " -> " + value), true);
        return 1;
    }
    private static int setSpellCasts(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String spellId = StringArgumentType.getString(ctx, "spell");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        if (SpellRegistry.byId(spellId) == null) {
            ctx.getSource().sendFailure(Component.literal("Sort inconnu : " + spellId));
            return 0;
        }
        player.getData(PlayerProgressionData.PROGRESSION).setSpellCasts(spellId, value);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Lancers de " + spellId + " pour " + player.getName().getString() + " -> " + value), true);
        return 1;
    }
}