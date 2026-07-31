package net.cosette.miyabi.magic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
public final class DamageLogCommands {
    private DamageLogCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("miyabi")
                .then(Commands.literal("magic")
                        .then(Commands.literal("damagelog")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(DamageLogCommands::setEnabled))))));
    }

    private static int setEnabled(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        player.getData(DamageLogData.DAMAGE_LOG).setEnabled(enabled);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Log de dégâts " + (enabled ? "activé" : "désactivé") + " pour " + player.getName().getString()), true);
        return 1;
    }
}