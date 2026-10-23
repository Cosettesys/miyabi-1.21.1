package net.cosette.miyabi;

import net.cosette.miyabi.config.MiyabiConfig;
import net.cosette.miyabi.progression.MeditationData;
import net.cosette.miyabi.progression.PlayerProgressionData;
import net.cosette.miyabi.registry.*;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.cosette.miyabi.skills.cast.CastData;
import net.cosette.miyabi.skills.damage.DamageLogData;
import net.cosette.miyabi.skills.mana.ManaData;
import net.cosette.miyabi.skills.mana.ManaDebugData;
import net.cosette.miyabi.skills.mana.MiyabiGameRules;
import net.cosette.miyabi.skills.damage.SchoolResistanceData;
import net.cosette.miyabi.skills.cast.SilentCastData;
import net.cosette.miyabi.skills.cast.SpellSlotsData;

@Mod(Miyabi.MODID)
public class Miyabi {
    public static final String MODID = "miyabi";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Miyabi(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModComponents.COMPONENTS.register(modEventBus);
        // Fichiers de configuration (config/miyabi) : créés au premier lancement, rechargeables avec /miyabi config reload
        MiyabiConfig.reload();
        MiyabiGameRules.init();

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
