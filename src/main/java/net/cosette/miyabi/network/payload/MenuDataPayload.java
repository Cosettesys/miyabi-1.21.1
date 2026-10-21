package net.cosette.miyabi.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

import java.util.ArrayList;
import java.util.List;

public record MenuDataPayload(
        String playerName, float mana, float maxMana, int auraLevel, boolean auraAwakened,
        List<SchoolEntry> schools, List<StyleEntry> styles, List<SpellEntry> spells,
        List<TechniqueEntry> techniques, List<String> slots, List<Float> auraProgress
) implements CustomPacketPayload {
    public record SchoolEntry(String school, String rank, int mastery, int required) {}
    public record StyleEntry(String style, String rank) {}
    /**
     * @param school2 deuxième école d'un sort hybride, ou "" ; le sort est listé dans le menu de chacune de ses écoles
     * @param mastery maîtrise du sort de 0 à 1 ; learning : jauge d'apprentissage de 0 à 1
     */
    public record SpellEntry(String id, String school, String school2, String rank, int casts, float manaCost, boolean learned,
                             float damage, float heal, float castSeconds, float maxIntensity, String physical,
                             float mastery, float learning, String description, String incantation) {}
    public record TechniqueEntry(String id, String style, String rank, boolean learned,
                                 float damage, float cooldownSeconds, float reach, String physical, String weapon,
                                 float mastery, float learning) {}
    public static final Type<MenuDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "menu_data"));
    public static final StreamCodec<FriendlyByteBuf, MenuDataPayload> STREAM_CODEC =
            StreamCodec.of(MenuDataPayload::write, MenuDataPayload::read);
    private static void write(FriendlyByteBuf buf, MenuDataPayload p) {
        buf.writeUtf(p.playerName());
        buf.writeFloat(p.mana());
        buf.writeFloat(p.maxMana());
        buf.writeVarInt(p.auraLevel());
        buf.writeBoolean(p.auraAwakened());
        buf.writeVarInt(p.schools().size());
        for (SchoolEntry e : p.schools()) {
            buf.writeUtf(e.school());
            buf.writeUtf(e.rank());
            buf.writeVarInt(e.mastery());
            buf.writeVarInt(e.required());
        }
        buf.writeVarInt(p.styles().size());
        for (StyleEntry e : p.styles()) {
            buf.writeUtf(e.style());
            buf.writeUtf(e.rank());
        }
        buf.writeVarInt(p.spells().size());
        for (SpellEntry e : p.spells()) {
            buf.writeUtf(e.id());
            buf.writeUtf(e.school());
            buf.writeUtf(e.school2());
            buf.writeUtf(e.rank());
            buf.writeVarInt(e.casts());
            buf.writeFloat(e.manaCost());
            buf.writeBoolean(e.learned());
            buf.writeFloat(e.damage());
            buf.writeFloat(e.heal());
            buf.writeFloat(e.castSeconds());
            buf.writeFloat(e.maxIntensity());
            buf.writeUtf(e.physical());
            buf.writeFloat(e.mastery());
            buf.writeFloat(e.learning());
            buf.writeUtf(e.description(), 4096);
            buf.writeUtf(e.incantation(), 4096);
        }
        buf.writeVarInt(p.techniques().size());
        for (TechniqueEntry e : p.techniques()) {
            buf.writeUtf(e.id());
            buf.writeUtf(e.style());
            buf.writeUtf(e.rank());
            buf.writeBoolean(e.learned());
            buf.writeFloat(e.damage());
            buf.writeFloat(e.cooldownSeconds());
            buf.writeFloat(e.reach());
            buf.writeUtf(e.physical());
            buf.writeUtf(e.weapon());
            buf.writeFloat(e.mastery());
            buf.writeFloat(e.learning());
        }
        buf.writeVarInt(p.slots().size());
        for (String id : p.slots()) {
            buf.writeUtf(id);
        }
        buf.writeVarInt(p.auraProgress().size());
        for (float ratio : p.auraProgress()) {
            buf.writeFloat(ratio);
        }
    }
    private static MenuDataPayload read(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        float mana = buf.readFloat();
        float maxMana = buf.readFloat();
        int aura = buf.readVarInt();
        boolean auraAwakened = buf.readBoolean();
        int schoolCount = buf.readVarInt();
        List<SchoolEntry> schools = new ArrayList<>();
        for (int i = 0; i < schoolCount; i++) {
            schools.add(new SchoolEntry(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt()));
        }
        int styleCount = buf.readVarInt();
        List<StyleEntry> styles = new ArrayList<>();
        for (int i = 0; i < styleCount; i++) {
            styles.add(new StyleEntry(buf.readUtf(), buf.readUtf()));
        }
        int spellCount = buf.readVarInt();
        List<SpellEntry> spells = new ArrayList<>();
        for (int i = 0; i < spellCount; i++) {
            spells.add(new SpellEntry(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt(),
                    buf.readFloat(), buf.readBoolean(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readUtf(),
                    buf.readFloat(), buf.readFloat(), buf.readUtf(4096), buf.readUtf(4096)));
        }
        int techniqueCount = buf.readVarInt();
        List<TechniqueEntry> techniques = new ArrayList<>();
        for (int i = 0; i < techniqueCount; i++) {
            techniques.add(new TechniqueEntry(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readUtf(), buf.readUtf(),
                    buf.readFloat(), buf.readFloat()));
        }
        int slotCount = buf.readVarInt();
        List<String> slots = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            slots.add(buf.readUtf());
        }
        int auraCount = buf.readVarInt();
        List<Float> auraProgress = new ArrayList<>();
        for (int i = 0; i < auraCount; i++) {
            auraProgress.add(buf.readFloat());
        }
        return new MenuDataPayload(name, mana, maxMana, aura, auraAwakened, schools, styles, spells, techniques, slots, auraProgress);
    }
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}