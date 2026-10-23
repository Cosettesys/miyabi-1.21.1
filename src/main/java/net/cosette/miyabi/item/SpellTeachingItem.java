package net.cosette.miyabi.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.cosette.miyabi.registry.ModComponents;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.skills.learning.LearnMethod;
import net.cosette.miyabi.skills.learning.SpellLearning;
import net.cosette.miyabi.skills.magic.SpellDefinition;
import net.cosette.miyabi.util.Chat;

import java.util.List;

/**
 * Objet qui enseigne un sort : parchemin (SCROLL) ou grimoire (GRIMOIRE). Le composant miyabi:taught_spell contient
 * l'id du sort. Un objet n'enseigne que les sorts dont le learnable_by (config du sort) correspond à son type.
 * Le lire donne la formule exacte du sort : il devient apprenable à vitesse normale (au lieu de la vitesse réduite d'un sort seulement vu).
 */
public class SpellTeachingItem extends Item {
    private final LearnMethod method;
    public SpellTeachingItem(Properties properties, LearnMethod method) {
        super(properties);
        this.method = method;
    }
    /** Un exemplaire de cet objet pour ce sort. */
    public static ItemStack create(Item item, String spellId) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModComponents.TAUGHT_SPELL.get(), spellId);
        return stack;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        String spellId = stack.get(ModComponents.TAUGHT_SPELL.get());
        SpellDefinition spell = spellId == null ? null : SpellRegistry.byId(spellId);
        if (spell == null) {
            serverPlayer.sendSystemMessage(Component.literal("Ce " + method.itemName() + " est " + Chat.bad("illisible") + "."));
            return InteractionResultHolder.fail(stack);
        }
        if (SpellLearning.readBook(serverPlayer, spell, method) && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String spellId = stack.get(ModComponents.TAUGHT_SPELL.get());
        SpellDefinition spell = spellId == null ? null : SpellRegistry.byId(spellId);
        if (spell == null) {
            tooltip.add(Component.literal("Vierge ou illisible"));
            return;
        }
        tooltip.add(Component.literal(Chat.schools(spell.schools()) + " : " + spell.id()));
        tooltip.add(Component.literal(Chat.dim("Rang " + spell.rank().name())));
    }
}