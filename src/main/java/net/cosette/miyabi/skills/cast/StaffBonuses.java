package net.cosette.miyabi.skills.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.cosette.miyabi.config.ConfigValue;
import net.cosette.miyabi.item.StaffItem;
import net.cosette.miyabi.skills.magic.MagicSchool;

import java.util.List;

/** Bonus des bâtons tenus en main principale ou secondaire. */
public final class StaffBonuses {
    private StaffBonuses() {}
    @ConfigValue(value = "staffs.max_cost_reduction", comment = "Réduction de coût maximale qu'un bâton peut donner (0,95 = le coût ne peut pas descendre sous 5 %).", min = 0, max = 0.99)
    public static float MAX_COST_REDUCTION = 0.95f;
    /** Meilleure réduction parmi les bâtons tenus pour une école. Elles ne s'additionnent pas. */
    private static float bestReduction(ServerPlayer player, MagicSchool school) {
        float best = 0f;
        for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (stack.getItem() instanceof StaffItem staff) {
                best = Math.max(best, staff.costReductionFor(school));
            }
        }
        return best;
    }
    /** Réduction de coût (0 à 1) pour un sort : pour un sort hybride, la moyenne de celles de ses écoles. */
    public static float costReduction(ServerPlayer player, List<MagicSchool> schools) {
        float sum = 0f;
        for (MagicSchool school : schools) {
            sum += bestReduction(player, school);
        }
        return Math.min(sum / schools.size(), MAX_COST_REDUCTION);
    }
    public static float costMultiplier(ServerPlayer player, List<MagicSchool> schools) {
        return 1f - costReduction(player, schools);
    }
}