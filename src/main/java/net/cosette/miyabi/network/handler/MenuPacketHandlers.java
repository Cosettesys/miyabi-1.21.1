package net.cosette.miyabi.network.handler;

import net.cosette.miyabi.registry.ModAttachments;
import net.cosette.miyabi.skills.battleaura.BattleAuraManager;
import net.cosette.miyabi.skills.magic.effect.DamageEffect;
import net.cosette.miyabi.skills.magic.effect.HealEffect;
import net.cosette.miyabi.skills.swordtechniques.TechniqueManager;
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
import net.cosette.miyabi.util.Chat;
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
            if (!player.getData(ModAttachments.PROGRESSION).isManaAwakened()) {
                player.sendSystemMessage(Component.literal("Ton " + Chat.mana("mana") + " n'est " + Chat.bad("pas éveillé") + "."));
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
            if (!player.getData(ModAttachments.PROGRESSION).isManaAwakened()) return;
            SpellDefinition spell = SpellRegistry.byId(payload.spellId());
            if (spell != null && !player.getData(ModAttachments.PROGRESSION).isSpellUnlocked(spell.id())) return;
            if (player.getData(ModAttachments.CAST).isCasting()
                    || player.getData(ModAttachments.MEDITATION).isMeditating()) {
                player.sendSystemMessage(Component.literal(
                        "Impossible de changer de sort pendant une incantation ou une méditation."));
                return;
            }
            player.getData(ModAttachments.SPELL_SLOTS).assign(payload.slot(), payload.spellId());
            sendSnapshot(player);
        });
    }
    // Client : affiche ou met à jour le menu. Passe par ClientHooks pour ne pas charger de classes client sur un serveur dédié.
    public static void onMenuData(MenuDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHooks.openMenu(payload));
    }
    private static void sendSnapshot(ServerPlayer player) {
        PlayerProgressionData progression = player.getData(ModAttachments.PROGRESSION);
        ManaData mana = player.getData(ModAttachments.MANA);
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
            if (!progression.isSpellUnlocked(def.id())) continue; // sort pas encore débloqué : invisible
            float damage = 0f;
            float heal = 0f;
            String physical = "NONE";
            if (def.effect() instanceof DamageEffect damageEffect) {
                damage = damageEffect.previewDamage(def, player);
                physical = damageEffect.physicalType().name();
            } else if (def.effect() instanceof HealEffect healEffect) {
                heal = healEffect.baseHeal();
            }
            spells.add(new MenuDataPayload.SpellEntry(
                    def.id(), def.school().name(), def.rank().name(),
                    progression.getSpellCasts(def.id()), CastManager.effectiveManaCost(player, def),
                    progression.isSpellLearned(def.id()),
                    damage, heal, CastManager.castSeconds(player, def), def.maxIntensityPercent(), physical));
        }
        List<SwordTechniqueDefinition> techs = new ArrayList<>(SwordTechniqueRegistry.all());
        techs.sort(Comparator.comparingInt((SwordTechniqueDefinition t) -> t.rank().ordinal())
                .thenComparing(SwordTechniqueDefinition::id));
        List<MenuDataPayload.TechniqueEntry> techniques = new ArrayList<>();
        for (SwordTechniqueDefinition t : techs) {
            float damage = t.baseDamage() * t.physicalType().getDamageMultiplier()
                    * progression.getStyleRank(t.style()).getDamageMultiplier();
            String weapon = t.requiredWeapon() == null ? "" : t.requiredWeapon().location().toString();
            techniques.add(new MenuDataPayload.TechniqueEntry(t.id(), t.style().name(), t.rank().name(),
                    damage, TechniqueManager.effectiveCooldownTicks(player, t) / 20f, t.reach(), t.physicalType().name(), weapon));
        }
        SpellSlotsData slotData = player.getData(ModAttachments.SPELL_SLOTS);
        List<String> slots = new ArrayList<>();
        for (int i = 0; i < SpellSlots.COUNT; i++) {
            slots.add(slotData.getSpellId(i));
        }
        List<Float> auraProgress = BattleAuraManager.progress(progression);
        PacketDistributor.sendToPlayer(player, new MenuDataPayload(
                player.getName().getString(), mana.getCurrent(), mana.getMax(),
                progression.getBattleAuraLevel(), progression.isBattleAuraAwakened(),
                schools, styles, spells, techniques, slots, auraProgress));
    }
}