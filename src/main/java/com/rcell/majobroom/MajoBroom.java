package com.rcell.majobroom;

import com.mojang.logging.LogUtils;
import com.rcell.majobroom.compat.CompatManager;
import com.rcell.majobroom.config.ServerConfig;
import com.rcell.majobroom.init.ModEntities;
import com.rcell.majobroom.init.ModItems;
import com.rcell.majobroom.network.ModNetwork;
import com.rcell.majobroom.init.ModCreativeTabs;
import com.rcell.majobroom.item.armor.MajoArmorMaterials;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MajoBroom.MODID)
public class MajoBroom
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "majobroom";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public MajoBroom(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MajoArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        // 使用SERVER类型：配置存储在服务端并同步到客户端
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        // Initialize GeckoLib (client+server safe)
        GeckoLib.initialize();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(ModNetwork::register);
        
        // 初始化模组兼容
        event.enqueueWork(CompatManager::init);
    }
    
    private void clientSetup(final FMLClientSetupEvent event)
    {
        // 注册物品 Tooltip（在物品注册完成后）
        event.enqueueWork(() -> {
            ModItems.registerTooltips();
            LOGGER.info("Item tooltips registered!");
        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // 客户端初始化（渲染注册请使用 EntityRenderersEvent.RegisterRenderers）
        }
    }
}
