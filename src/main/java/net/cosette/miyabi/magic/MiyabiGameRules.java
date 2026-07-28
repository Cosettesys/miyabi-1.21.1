package net.cosette.miyabi.magic;

import net.minecraft.world.level.GameRules;

public final class MiyabiGameRules {
    private MiyabiGameRules() {}
    public static final GameRules.Key<GameRules.IntegerValue> MANA_REGEN_TYPE =
            GameRules.register(
                    "miyabiManaRegenType",
                    GameRules.Category.MISC,
                    GameRules.IntegerValue.create(0)
            );
    public static void init() {}
}