package net.cosette.miyabi.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.cosette.miyabi.skills.cast.SilentCastData;
import net.cosette.miyabi.skills.damage.DamageLogData;
import net.cosette.miyabi.skills.damage.SchoolResistanceData;
import net.cosette.miyabi.skills.magic.MagicSchool;

import java.util.Collection;

/** Branche /miyabi magic ... (silentcast, damagelog, resistance) */
public final class MagicCommands {
    private MagicCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("magic")
                .requires(CommandUtil::isOp)
                .then(CommandUtil.playerValue("silentcast", "unlocked", BoolArgumentType.bool(), MagicCommands::setSilentCastUnlocked))
                .then(CommandUtil.playerValue("damagelog", "enabled", BoolArgumentType.bool(), MagicCommands::setDamageLog))
                .then(Commands.literal("resistance")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(CommandUtil.enumArg("school", MagicSchool.class)
                                        .then(Commands.argument("percent", FloatArgumentType.floatArg())
                                                .executes(MagicCommands::setResistance)))));
    }

    private static int setSilentCastUnlocked(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        boolean unlocked = BoolArgumentType.getBool(ctx, "unlocked");
        player.getData(SilentCastData.SILENT_CAST).setUnlocked(unlocked);
        return CommandUtil.success(ctx, "Silent cast " + (unlocked ? "débloqué" : "verrouillé") + " pour " + player.getName().getString());
    }

    private static int setDamageLog(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = CommandUtil.player(ctx);
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        player.getData(DamageLogData.DAMAGE_LOG).setEnabled(enabled);
        return CommandUtil.success(ctx, "Log de dégâts " + (enabled ? "activé" : "désactivé") + " pour " + player.getName().getString());
    }

    private static int setResistance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "target");
        String raw = StringArgumentType.getString(ctx, "school");
        float percent = FloatArgumentType.getFloat(ctx, "percent");

        MagicSchool school = CommandUtil.parseEnum(MagicSchool.class, raw);
        if (school == null) {
            return CommandUtil.fail(ctx, "École inconnue : " + raw.toUpperCase());
        }

        for (Entity target : targets) {
            target.getData(SchoolResistanceData.SCHOOL_RESISTANCE).setResistancePercent(school, percent);
        }
        CommandUtil.success(ctx, targets.size() + " entité(s) -> résistance " + school.name() + " = " + percent + "%");
        return targets.size();
    }
}
