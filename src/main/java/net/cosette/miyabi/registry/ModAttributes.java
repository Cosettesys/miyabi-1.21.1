package net.cosette.miyabi.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.Miyabi;

@EventBusSubscriber(modid = Miyabi.MODID)
public final class ModAttributes {
    private ModAttributes() {}
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, Miyabi.MODID);
    /**
     * Vitesse d'incantation : 1 = normale, 2 = deux fois plus vite (temps de cast divisé par deux).
     * Utilisable par des objets (bâtons), des effets de potion, etc., comme les dégâts d'attaque pour un outil.
     */
    public static final DeferredHolder<Attribute, Attribute> CASTING_SPEED = ATTRIBUTES.register("casting_speed",
            () -> new RangedAttribute("attribute.name.miyabi.casting_speed", 1.0, 0.1, 100.0).setSyncable(true));
    /** Ajoute la vitesse d'incantation aux joueurs. */
    @SubscribeEvent
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CASTING_SPEED);
    }
}