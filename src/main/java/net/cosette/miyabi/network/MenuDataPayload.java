package net.cosette.miyabi.network;

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
        List<TechniqueEntry> techniques, List<String> slots
) implements CustomPacketPayload {
    public record SchoolEntry(String school, String rank, int mastery, int required) {}
    public record StyleEntry(String style, String rank) {}
    public record SpellEntry(String id, String school, String rank, int casts, float manaCost) {}
    public record TechniqueEntry(String id, String style, String rank) {}
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
            buf.writeUtf(e.rank());
            buf.writeVarInt(e.casts());
            buf.writeFloat(e.manaCost());
        }
        buf.writeVarInt(p.techniques().size());
        for (TechniqueEntry e : p.techniques()) {
            buf.writeUtf(e.id());
            buf.writeUtf(e.style());
            buf.writeUtf(e.rank());
        }
        buf.writeVarInt(p.slots().size());
        for (String id : p.slots()) {
            buf.writeUtf(id);
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
            spells.add(new SpellEntry(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readFloat()));
        }
        int techniqueCount = buf.readVarInt();
        List<TechniqueEntry> techniques = new ArrayList<>();
        for (int i = 0; i < techniqueCount; i++) {
            techniques.add(new TechniqueEntry(buf.readUtf(), buf.readUtf(), buf.readUtf()));
        }
        int slotCount = buf.readVarInt();
        List<String> slots = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            slots.add(buf.readUtf());
        }
        return new MenuDataPayload(name, mana, maxMana, aura, auraAwakened, schools, styles, spells, techniques, slots);
    }
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}