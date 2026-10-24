package net.cosette.miyabi.command;

import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.cosette.miyabi.Miyabi;

/**
 * Point d'entrée unique : enregistre la racine /miyabi et y branche chaque famille de commandes.
 * progression : éveils et rangs | stats : mana et battle aura | spell et technique : compétences | magic : réglages de test | config.
 */
@EventBusSubscriber(modid = Miyabi.MODID)
public final class MiyabiCommands {
    private MiyabiCommands() {}
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("miyabi")
                .then(ProgressionCommands.branch())
                .then(StatsCommands.branch())
                .then(SpellCommands.branch())
                .then(TechniqueCommands.branch())
                .then(MagicCommands.branch())
                .then(ConfigCommands.branch())
                .then(QuestCommands.branch()));

    }
}