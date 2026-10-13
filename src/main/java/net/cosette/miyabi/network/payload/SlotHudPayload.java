package net.cosette.miyabi.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.Miyabi;

import java.util.ArrayList;
import java.util.List;

/** Serveur -> client : contenu des slots de la barre, tel qu'affiché dans le HUD. */
public record SlotHudPayload(List<Entry> slots) implements CustomPacketPayload {
    public static final int KIND_EMPTY = 0;
    public static final int KIND_SPELL = 1;      // value = coût en mana
    public static final int KIND_TECHNIQUE = 2;  // value = recharge en secondes
    public record Entry(int kind, float value, int remaining) {}
    public static final Type<SlotHudPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "slot_hud"));
    public static final StreamCodec<FriendlyByteBuf, SlotHudPayload> STREAM_CODEC =
            StreamCodec.of(SlotHudPayload::write, SlotHudPayload::read);
    private static void write(FriendlyByteBuf buf, SlotHudPayload p) {
        buf.writeVarInt(p.slots().size());
        for (Entry e : p.slots()) {
            buf.writeVarInt(e.kind());
            buf.writeFloat(e.value());
            buf.writeVarInt(e.remaining());
        }
    }
    private static SlotHudPayload read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Entry> slots = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            slots.add(new Entry(buf.readVarInt(), buf.readFloat(), buf.readVarInt()));
        }
        return new SlotHudPayload(slots);
    }
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}