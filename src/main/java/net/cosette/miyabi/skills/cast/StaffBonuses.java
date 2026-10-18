package net.cosette.miyabi.skills.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.cosette.miyabi.item.StaffItem;
import net.cosette.miyabi.skills.magic.MagicSchool;

/** Bonus des bâtons tenus en main principale ou secondaire. */
public final class StaffBonuses {
    private StaffBonuses() {}
    /** Une réduction ne peut pas faire tomber le coût à zéro. */
    private static final float MAX_COST_REDUCTION = 0.95f;
    /** Meilleure réduction de coût (0 à 1) parmi les bâtons tenus, pour cette école. Elles ne s'additionnent pas. */
    public static float costReduction(ServerPlayer player, MagicSchool school) {
        float best = 0f;
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.getItem() instanceof StaffItem staff) {
                best = Math.max(best, staff.costReductionFor(school));
            }
        }
        return Math.min(best, MAX_COST_REDUCTION);
    }
    public static float costMultiplier(ServerPlayer player, MagicSchool school) {
        return 1f - costReduction(player, school);
    }
}