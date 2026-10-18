package net.cosette.miyabi.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.cosette.miyabi.skills.magic.MagicSchool;

import java.util.List;
/**
 * Bâton magique : tenu en main principale ou secondaire, il réduit le coût en mana des sorts, plus fortement pour son
 * école cible. La vitesse d'incantation vient de l'attribut miyabi:casting_speed porté par l'objet (voir ModItems).
 */
public class StaffItem extends Item {
    private final MagicSchool targetSchool;
    private final float targetCostReduction; // 0.8 = coût réduit de 80 %
    private final float otherCostReduction;
    public StaffItem(Properties properties, MagicSchool targetSchool, float targetCostReduction, float otherCostReduction) {
        super(properties);
        this.targetSchool = targetSchool;
        this.targetCostReduction = targetCostReduction;
        this.otherCostReduction = otherCostReduction;
    }
    /** Réduction du coût en mana (0 à 1) pour un sort de cette école. */
    public float costReductionFor(MagicSchool school) {
        return school == targetSchool ? targetCostReduction : otherCostReduction;
    }
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Magie " + targetSchool.name() + " : coût -" + Math.round(targetCostReduction * 100) + " %")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Autres écoles : coût -" + Math.round(otherCostReduction * 100) + " %")
                .withStyle(ChatFormatting.GRAY));
    }
}