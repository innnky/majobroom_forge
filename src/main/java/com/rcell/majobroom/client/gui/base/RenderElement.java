package com.rcell.majobroom.client.gui.base;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 可渲染元素接口
 * 替代 catnip 的 ScreenElement
 */
@FunctionalInterface
public interface RenderElement {
    /**
     * 渲染元素
     * @param graphics 渲染上下文
     * @param x X坐标
     * @param y Y坐标
     */
    void render(GuiGraphics graphics, int x, int y);
}

