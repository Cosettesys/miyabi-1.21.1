package net.cosette.miyabi.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

/** Briques communes à toutes les branches de la commande /miyabi. */
public final class CommandUtil {
    private CommandUtil() {}

    public static boolean isOp(CommandSourceStack source) {
        return source.hasPermission(2);
    }

    /** Le joueur désigné par l'argument "player". */
    public static ServerPlayer player(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return EntityArgument.getPlayer(ctx, "player");
    }

    /** Message de succès, diffusé aux opérateurs. */
    public static int success(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    /** Message d'information, visible uniquement par celui qui a tapé la commande. */
    public static int info(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    public static int fail(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendFailure(Component.literal(message));
        return 0;
    }

    /** Valeur d'un enum à partir d'un texte (insensible à la casse), ou null si inconnue. */
    public static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        try {
            return Enum.valueOf(type, raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Argument texte avec auto-complétion des valeurs d'un enum. */
    public static <E extends Enum<E>> RequiredArgumentBuilder<CommandSourceStack, String> enumArg(String name, Class<E> type) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(type.getEnumConstants()).map(Enum::name), builder));
    }

    /** Forme la plus courante des commandes de réglage : nom + joueur + une valeur. */
    public static <T> LiteralArgumentBuilder<CommandSourceStack> playerValue(
            String literal, String valueName, ArgumentType<T> valueType, Command<CommandSourceStack> executes) {
        return Commands.literal(literal)
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument(valueName, valueType)
                                .executes(executes)));
    }
}
