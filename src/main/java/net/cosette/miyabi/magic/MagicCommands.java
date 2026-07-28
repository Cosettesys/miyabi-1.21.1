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
public final class MagicCommands {
    private MagicCommands() {}
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }
    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("miyabi")
                .then(Commands.literal("magic")
                        .then(Commands.literal("silentcast")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("unlocked", BoolArgumentType.bool())
                                                .executes(MagicCommands::setSilentCastUnlocked))))));
    }
    private static int setSilentCastUnlocked(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        boolean unlocked = BoolArgumentType.getBool(ctx, "unlocked");
        player.getData(SilentCastData.SILENT_CAST).setUnlocked(unlocked);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Silent cast " + (unlocked ? "débloqué" : "verrouillé") + " pour " + player.getName().getString()), true);
        return 1;
    }
}