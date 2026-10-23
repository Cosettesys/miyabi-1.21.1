package net.cosette.miyabi.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.cosette.miyabi.config.ClientConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.cosette.miyabi.config.ConfigReport;
import net.cosette.miyabi.config.MiyabiConfig;
import net.cosette.miyabi.util.Chat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/** Branche /miyabi config ... */
public final class ConfigCommands {
    private ConfigCommands() {}
    private static final int MAX_WARNINGS_SHOWN = 5;
    public static LiteralArgumentBuilder<CommandSourceStack> branch() {
        return Commands.literal("config")
                .requires(CommandUtil::isOp)
                .then(Commands.literal("reload").executes(ConfigCommands::reload));
    }
    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfigReport report = MiyabiConfig.reload();
        // En solo, le client et le serveur partagent la machine : on recharge aussi le fichier client
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientConfig.reload();
        }
        StringBuilder message = new StringBuilder(Chat.title("Configuration rechargée"))
                .append(" : ").append(Chat.good(report.files())).append(" fichier(s)");
        if (report.added() > 0) {
            message.append(", ").append(Chat.mana(report.added())).append(" valeur(s) ajoutée(s) aux fichiers");
        }
        if (!report.warnings().isEmpty()) {
            message.append("\n").append(Chat.bad(report.warnings().size() + " avertissement(s)")).append(" :");
            int shown = Math.min(MAX_WARNINGS_SHOWN, report.warnings().size());
            for (int i = 0; i < shown; i++) {
                message.append("\n - ").append(Chat.dim(report.warnings().get(i)));
            }
            if (report.warnings().size() > shown) {
                message.append("\n - ").append(Chat.dim("... voir les logs du serveur pour la suite"));
            }
        }
        ctx.getSource().sendSuccess(() -> Component.literal(message.toString()), true);
        return 1;
    }
}