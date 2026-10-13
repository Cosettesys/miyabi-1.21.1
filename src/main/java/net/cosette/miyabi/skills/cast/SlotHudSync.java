package net.cosette.miyabi.skills.cast;

import net.cosette.miyabi.skills.swordtechniques.TechniqueCooldownData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.network.payload.SlotHudPayload;
import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.skills.swordtechniques.TechniqueManager;

import java.util.ArrayList;
import java.util.List;

/** Envoie au client le contenu des slots (coût en mana ou recharge) dès qu'il change : assignation, maîtrise... */
@EventBusSubscriber(modid = Miyabi.MODID)
public final class SlotHudSync {
    private SlotHudSync() {}
    private static int ceilSeconds(long ticks) {
        return (int) Math.ceil(ticks / 20.0);
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SlotHudPayload payload = build(player);
        SlotHudSyncState state = player.getData(ModAttachments.SLOT_HUD_SYNC);
        if (!payload.equals(state.getLastSent())) {
            PacketDistributor.sendToPlayer(player, payload);
            state.setLastSent(payload);
        }
    }
    private static SlotHudPayload build(ServerPlayer player) {
        SpellSlotsData slotData = player.getData(ModAttachments.SPELL_SLOTS);
        TechniqueCooldownData cooldown = player.getData(ModAttachments.TECHNIQUE_COOLDOWN);
        long now = player.level().getGameTime();
        List<SlotHudPayload.Entry> entries = new ArrayList<>();
        for (int i = 0; i < SpellSlots.COUNT; i++) {
            String id = slotData.getSpellId(i);
            SpellDefinition spell = id.isEmpty() ? null : SpellRegistry.byId(id);
            SwordTechniqueDefinition technique = id.isEmpty() ? null : SwordTechniqueRegistry.byId(id);
            if (spell != null) {
                entries.add(new SlotHudPayload.Entry(SlotHudPayload.KIND_SPELL, CastManager.effectiveManaCost(player, spell), 0));
            } else if (technique != null) {
                entries.add(new SlotHudPayload.Entry(SlotHudPayload.KIND_TECHNIQUE,
                        ceilSeconds(TechniqueManager.effectiveCooldownTicks(player, technique)),
                        ceilSeconds(cooldown.remainingTicks(technique.id(), now))));
            } else {
                entries.add(new SlotHudPayload.Entry(SlotHudPayload.KIND_EMPTY, 0f, 0));
            }
        }
        return new SlotHudPayload(entries);
    }

}