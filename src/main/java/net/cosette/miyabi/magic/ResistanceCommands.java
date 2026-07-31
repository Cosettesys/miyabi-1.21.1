package net.cosette.miyabi.magic;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.magic.spell.SpellElement;

import java.util.Arrays;
import java.util.Collection;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ResistanceCommands {
    private ResistanceCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("miyabi")
                .then(Commands.literal("magic")
                        .then(Commands.literal("resistance")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("target", EntityArgument.entities())
                                        .then(Commands.argument("element", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(SpellElement.values()).map(Enum::name), builder))
                                                .then(Commands.argument("percent", FloatArgumentType.floatArg())
                                                        .executes(ResistanceCommands::setResistance)))))));
    }

    private static int setResistance(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");
        String elementName = StringArgumentType.getString(ctx, "element").toUpperCase();
        float percent = FloatArgumentType.getFloat(ctx, "percent");

        SpellElement element;
        try {
            element = SpellElement.valueOf(elementName);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("Élément inconnu : " + elementName));
            return 0;
        }

        for (Entity target : targets) {
            target.getData(ElementResistanceData.ELEMENT_RESISTANCE).setResistancePercent(element, percent);
        }

        int count = targets.size();
        SpellElement finalElement = element;
        ctx.getSource().sendSuccess(() -> Component.literal(count + " entité(s) -> résistance " + finalElement.name() + " = " + percent + "%"), true);
        return count;
    }
}