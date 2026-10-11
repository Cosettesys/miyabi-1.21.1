package net.cosette.miyabi.network.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.cosette.miyabi.client.screen.ClientHooks;
import net.cosette.miyabi.skills.cast.CastData;
import net.cosette.miyabi.skills.cast.CastManager;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.skills.cast.SpellSlots;
import net.cosette.miyabi.skills.cast.SpellSlotsData;
import net.cosette.miyabi.skills.magic.MagicSchool;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.MeditationData;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.swordtechniques.SwordTechniqueDefinition;
import net.cosette.miyabi.skills.SwordTechniqueRegistry;
import java.util.Comparator;

import java.util.ArrayList;
import java.util.List;
import net.cosette.miyabi.network.payload.MenuDataPayload;
import net.cosette.miyabi.network.payload.MenuRequestPayload;
import net.cosette.miyabi.network.payload.SlotAssignPayload;

public final class MenuPacketHandlers {
    private MenuPacketHandlers() {}
    // Serveur : demande d'ouverture du menu.
    public static void onMenuRequest(MenuRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!player.getData(PlayerProgressionData.PROGRESSION).isManaAwakened()) {
                player.sendSystemMessage(Component.literal("Ton mana n'est pas éveillé."));
                return;
            }
            sendSnapshot(player);
        });
    }
    // Serveur : assignation d'un sort à un slot.
    public static void onSlotAssign(SlotAssignPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (payload.slot() < 0 || payload.slot() >= SpellSlots.COUNT) return;
            if (SpellRegistry.byId(payload.spellId()) == null
                    && SwordTechniqueRegistry.byId(payload.spellId()) == null) return;
            if (!player.getData(PlayerProgressionData.PROGRESSION).isManaAwakened()) return;
            if (player.getData(CastData.CAST).isCasting()
                    || player.getData(MeditationData.MEDITATION).isMeditating()) {
                player.sendSystemMessage(Component.literal(
                        "Impossible de changer de sort pendant une incantation ou une méditation."));
                return;
            }
            player.getData(SpellSlotsData.SPELL_SLOTS).assign(payload.slot(), payload.spellId());
            sendSnapshot(player);
        });
    }
    // Client : affiche ou met à jour le menu. Passe par ClientHooks pour ne pas charger de classes client sur un serveur dédié.
    public static void onMenuData(MenuDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHooks.openMenu(payload));
    }
    private static void sendSnapshot(ServerPlayer player) {
        PlayerProgressionData progression = player.getData(PlayerProgressionData.PROGRESSION);
        ManaData mana = player.getData(ManaData.MANA);
        List<MenuDataPayload.SchoolEntry> schools = new ArrayList<>();
        for (MagicSchool school : MagicSchool.values()) {
            MagicRank rank = progression.getSchoolRank(school);
            schools.add(new MenuDataPayload.SchoolEntry(
                    school.name(), rank.name(), progression.getSchoolMastery(school), rank.getMasteryToNext()));
        }
        List<MenuDataPayload.StyleEntry> styles = new ArrayList<>();
        for (SwordStyle style : SwordStyle.values()) {
            styles.add(new MenuDataPayload.StyleEntry(style.name(), progression.getStyleRank(style).name()));
        }
        List<SpellDefinition> defs = new ArrayList<>();
        for (String id : SpellRegistry.ids()) {
            defs.add(SpellRegistry.byId(id));
        }
        defs.sort(Comparator.comparingInt((SpellDefinition d) -> d.rank().ordinal()).thenComparing(SpellDefinition::id));
        List<MenuDataPayload.SpellEntry> spells = new ArrayList<>();
        for (SpellDefinition def : defs) {
            spells.add(new MenuDataPayload.SpellEntry(
                    def.id(), def.school().name(), def.rank().name(),
                    progression.getSpellCasts(def.id()), CastManager.effectiveManaCost(player, def)));
        }
        List<SwordTechniqueDefinition> techs = new ArrayList<>(SwordTechniqueRegistry.all());
        techs.sort(Comparator.comparingInt((SwordTechniqueDefinition t) -> t.rank().ordinal())
                .thenComparing(SwordTechniqueDefinition::id));
        List<MenuDataPayload.TechniqueEntry> techniques = new ArrayList<>();
        for (SwordTechniqueDefinition t : techs) {
            techniques.add(new MenuDataPayload.TechniqueEntry(t.id(), t.style().name(), t.rank().name()));
        }
        SpellSlotsData slotData = player.getData(SpellSlotsData.SPELL_SLOTS);
        List<String> slots = new ArrayList<>();
        for (int i = 0; i < SpellSlots.COUNT; i++) {
            slots.add(slotData.getSpellId(i));
        }
        PacketDistributor.sendToPlayer(player, new MenuDataPayload(
                player.getName().getString(), mana.getCurrent(), mana.getMax(),
                progression.getBattleAuraLevel(), progression.isBattleAuraAwakened(),
                schools, styles, spells, techniques, slots));
    }
}