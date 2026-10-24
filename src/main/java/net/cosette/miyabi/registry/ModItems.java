package net.cosette.miyabi.registry;

import net.cosette.miyabi.item.SpellTeachingItem;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.SpellRegistry;
import net.cosette.miyabi.skills.learning.LearnMethod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.Miyabi;
import net.cosette.miyabi.item.StaffItem;
import net.cosette.miyabi.skills.magic.MagicSchool;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ModItems {
    private ModItems() {}
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Miyabi.MODID);
    /**
     * Aqua Heartia : eau -80 % de coût, autres écoles -25 %, et vitesse d'incantation x2 (temps divisé par deux)
     * dans les deux mains. Le modificateur est "+100 % de la valeur de base" : 1 x (1 + 1,0) = 2.
     */
    public static final DeferredItem<StaffItem> AQUA_HEARTIA = ITEMS.register("aqua_heartia", () -> new StaffItem(
            new Item.Properties().stacksTo(1).attributes(ItemAttributeModifiers.builder()
                    .add(ModAttributes.CASTING_SPEED,
                            new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(Miyabi.MODID, "aqua_heartia_casting_speed"),
                                    1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                            EquipmentSlotGroup.HAND)
                    .build()),
            MagicSchool.WATER, 0.80f, 0.25f));
    /** Parchemin de sort : le sort enseigné est dans le composant miyabi:taught_spell. */
    /** Parchemin de sort : le sort enseigné est dans le composant miyabi:taught_spell. */
    public static final DeferredItem<SpellTeachingItem> SPELL_SCROLL =
            ITEMS.register("spell_scroll", () -> new SpellTeachingItem(new Item.Properties().stacksTo(16), LearnMethod.SCROLL));
    /** Grimoire : comme le parchemin, mais pour les sorts les plus puissants (learnable_by = GRIMOIRE). */
    public static final DeferredItem<SpellTeachingItem> GRIMOIRE =
            ITEMS.register("grimoire", () -> new SpellTeachingItem(new Item.Properties().stacksTo(1), LearnMethod.GRIMOIRE));
    /** Oeuf d'apparition de l'entraîneur. */
    /** Données d'entité d'un œuf d'entraîneur : le style qu'il enseignera. */
    private static Item.Properties trainerEgg(SwordStyle style) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Style", style.name());
        return new Item.Properties().component(DataComponents.ENTITY_DATA, CustomData.of(tag));
    }
    /** Oeufs d'apparition des trois entraîneurs, un par style. */
    public static final DeferredItem<DeferredSpawnEggItem> TRAINER_SWORD_GOD_EGG =
            ITEMS.register("trainer_sword_god_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.TRAINER, 0xB03030, 0xE8E0C0, trainerEgg(SwordStyle.SWORD_GOD)));
    public static final DeferredItem<DeferredSpawnEggItem> TRAINER_NORTH_GOD_EGG =
            ITEMS.register("trainer_north_god_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.TRAINER, 0x3A6EA5, 0xE8E0C0, trainerEgg(SwordStyle.NORTH_GOD)));
    public static final DeferredItem<DeferredSpawnEggItem> TRAINER_WATER_GOD_EGG =
            ITEMS.register("trainer_water_god_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.TRAINER, 0x3AA8C7, 0xE8E0C0, trainerEgg(SwordStyle.WATER_GOD)));
    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(AQUA_HEARTIA);
            // Un parchemin ou un grimoire par sort, selon son learnable_by
            for (String spellId : SpellRegistry.ids()) {
                LearnMethod method = SpellRegistry.byId(spellId).learnableBy();
                if (method == LearnMethod.SCROLL) {
                    event.accept(SpellTeachingItem.create(SPELL_SCROLL.get(), spellId));
                } else if (method == LearnMethod.GRIMOIRE) {
                    event.accept(SpellTeachingItem.create(GRIMOIRE.get(), spellId));
                }
            }
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(TRAINER_SWORD_GOD_EGG);
            event.accept(TRAINER_NORTH_GOD_EGG);
            event.accept(TRAINER_WATER_GOD_EGG);
        }
    }
}