package com.rcell.majobroom.client.gui.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

/**
 * 简单的屏幕打开工具
 * 替代 catnip 的 ScreenOpener
 */
public class SimpleScreenOpener {
    
    /**
     * 打开指定屏幕
     */
    public static void open(@Nullable Screen screen) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(screen);
    }
    
    /**
     * 关闭当前屏幕
     */
    public static void close() {
        open(null);
    }
}

