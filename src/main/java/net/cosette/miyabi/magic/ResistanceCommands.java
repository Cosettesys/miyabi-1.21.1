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
import net.cosette.miyabi.magic.spell.MagicSchool;
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
                                        .then(Commands.argument("school", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(MagicSchool.values()).map(Enum::name), builder))
                                                .then(Commands.argument("percent", FloatArgumentType.floatArg())
                                                        .executes(ResistanceCommands::setResistance)))))));
    }
    private static int setResistance(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");
        String schoolName = StringArgumentType.getString(ctx, "school").toUpperCase();
        float percent = FloatArgumentType.getFloat(ctx, "percent");
        MagicSchool school;
        try {
            school = MagicSchool.valueOf(schoolName);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("École inconnue : " + schoolName));
            return 0;
        }
        for (Entity target : targets) {
            target.getData(SchoolResistanceData.SCHOOL_RESISTANCE).setResistancePercent(school, percent);
        }
        int count = targets.size();
        MagicSchool finalSchool = school;
        ctx.getSource().sendSuccess(() -> Component.literal(count + " entité(s) -> résistance " + finalSchool.name() + " = " + percent + "%"), true);
        return count;
    }
}