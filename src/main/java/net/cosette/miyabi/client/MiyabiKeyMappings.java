package net.cosette.miyabi.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public final class MiyabiKeyMappings {
    private MiyabiKeyMappings() {}
    public static final String CATEGORY = "key.categories.miyabi";
    public static final KeyMapping SPELL_1 = new KeyMapping("key.miyabi.spell1", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping SPELL_2 = new KeyMapping("key.miyabi.spell2", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_Z, CATEGORY);
    public static final KeyMapping SPELL_3 = new KeyMapping("key.miyabi.spell3", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_X, CATEGORY);
    public static final KeyMapping SPELL_4 = new KeyMapping("key.miyabi.spell4", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_C, CATEGORY);
    public static final KeyMapping SPELL_5 = new KeyMapping("key.miyabi.spell5", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, CATEGORY);
    public static final KeyMapping SILENT_CAST_TOGGLE = new KeyMapping("key.miyabi.silentcast_toggle", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_G, CATEGORY);
    public static final KeyMapping[] SPELL_KEYS = { SPELL_1, SPELL_2, SPELL_3, SPELL_4, SPELL_5 };
}