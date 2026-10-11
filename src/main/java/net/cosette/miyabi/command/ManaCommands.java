package net.cosette.miyabi.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.skills.mana.ManaDebugData;

/** Branche /miyabi mana ... */
public final class ManaCommands {
    private ManaCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("mana")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ManaCommands::get)))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(intField("current"))
                                .then(intField("max"))
                                .then(intField("regenFlat"))
                                .then(Commands.literal("regenScaling")
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 100f))
                                                .executes(ManaCommands::setRegenScaling)))))
                .then(CommandUtil.playerValue("debuglog", "enabled", BoolArgumentType.bool(), ManaCommands::setDebugLog));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> intField(String name) {
        return Commands.literal(name)
                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> setIntField(ctx, name, IntegerArgumentType.getInteger(ctx, "value"))));
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        ManaData data = player.getData(ManaData.MANA);
        return CommandUtil.info(ctx, player.getName().getString() + " -> current=" + data.getCurrent()
                + ", max=" + data.getMax()
                + ", regenFlat=" + data.getRegenFlat()
                + ", regenScaling=" + data.getRegenScaling() + "%");
    }

    private static int setIntField(CommandContext<CommandSourceStack> ctx, String field, int value) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        ManaData data = player.getData(ManaData.MANA);
        switch (field) {
            case "current" -> data.setCurrent(value);
            case "max" -> data.setMax(value);
            case "regenFlat" -> data.setRegenFlat(value);
        }
        return CommandUtil.success(ctx, field + " de " + player.getName().getString() + " -> " + value);
    }

    private static int setRegenScaling(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        float value = FloatArgumentType.getFloat(ctx, "value");
        player.getData(ManaData.MANA).setRegenScaling(value);
        return CommandUtil.success(ctx, "regenScaling de " + player.getName().getString() + " -> " + value + "%");
    }

    private static int setDebugLog(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        player.getData(ManaDebugData.MANA_DEBUG).setEnabled(enabled);
        return CommandUtil.success(ctx, "Log mana " + (enabled ? "activé" : "désactivé") + " pour " + player.getName().getString());
    }
}
