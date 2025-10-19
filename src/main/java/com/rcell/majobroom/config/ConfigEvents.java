package com.rcell.majobroom.config;

import com.rcell.majobroom.MajoBroom;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * 配置事件监听器
 * 当配置加载或重新加载时，将配置值烘焙到缓存变量中
 */
@EventBusSubscriber(modid = MajoBroom.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigEvents {
    
    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        ServerConfig.bake();
    }
    
    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        ServerConfig.bake();
    }
}
