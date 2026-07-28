package net.cosette.miyabi.magic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.cosette.miyabi.Miyabi;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ManaCommands {
    private ManaCommands() {}
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }
    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("miyabi")
                .then(Commands.literal("mana")
                        .then(Commands.literal("get")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ManaCommands::get)))
                        .then(Commands.literal("set")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(intField("current"))
                                        .then(intField("max"))
                                        .then(intField("regenFlat"))
                                        .then(Commands.literal("regenScaling")
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 100f))
                                                        .executes(ctx -> setRegenScaling(ctx, FloatArgumentType.getFloat(ctx, "value"))))))))
        );
    }
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> intField(String name) {
        return Commands.literal(name)
                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> setIntField(ctx, name, IntegerArgumentType.getInteger(ctx, "value"))));
    }
    private static int get(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ManaData data = player.getData(ManaData.MANA);
        ctx.getSource().sendSuccess(() -> Component.literal(
                player.getName().getString() + " -> current=" + data.getCurrent()
                        + ", max=" + data.getMax()
                        + ", regenFlat=" + data.getRegenFlat()
                        + ", regenScaling=" + data.getRegenScaling() + "%"
        ), false);
        return 1;
    }
    private static int setIntField(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String field, int value)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ManaData data = player.getData(ManaData.MANA);
        switch (field) {
            case "current" -> data.setCurrent(value);
            case "max" -> data.setMax(value);
            case "regenFlat" -> data.setRegenFlat(value);
        }
        ctx.getSource().sendSuccess(() -> Component.literal(field + " de " + player.getName().getString() + " -> " + value), true);
        return 1;
    }
    private static int setRegenScaling(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, float value)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ManaData data = player.getData(ManaData.MANA);
        data.setRegenScaling(value);
        ctx.getSource().sendSuccess(() -> Component.literal("regenScaling de " + player.getName().getString() + " -> " + value + "%"), true);
        return 1;
    }
}