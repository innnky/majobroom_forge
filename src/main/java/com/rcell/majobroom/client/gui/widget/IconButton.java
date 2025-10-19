package com.rcell.majobroom.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rcell.majobroom.client.gui.base.BaseWidget;
import com.rcell.majobroom.client.gui.base.RenderElement;
import com.rcell.majobroom.client.gui.texture.GuiTextures;
import com.rcell.majobroom.client.gui.util.AllKeys;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

/**
 * 图标按钮控件
 * 完全参照 Create 的 IconButton 实现
 */
public class IconButton extends BaseWidget {
    protected RenderElement icon;
    public boolean green;
    public IconButton(int x, int y, RenderElement icon) {
        this(x, y, 18, 18, icon);
    }

    public IconButton(int x, int y, int w, int h, RenderElement icon) {
        super(x, y, w, h);
        this.icon = icon;
    }

    /**
     * 设置点击回调
     */
    @SuppressWarnings("unchecked")
    public IconButton withCallback(Runnable onClick) {
        return super.withCallback(onClick);
    }

    @Override
    public void doRender(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;

            GuiTextures button = !active ? GuiTextures.BUTTON_DISABLED
                : isHovered && AllKeys.isMouseButtonDown(0) ? GuiTextures.BUTTON_DOWN
                    : isHovered ? GuiTextures.BUTTON_HOVER
                        : green ? GuiTextures.BUTTON_GREEN : GuiTextures.BUTTON;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawBg(graphics, button);
            icon.render(graphics, getX() + 1, getY() + 1);
        }
    }

    protected void drawBg(GuiGraphics graphics, GuiTextures button) {
        graphics.blit(button.getLocation(), getX(), getY(), button.getStartX(), button.getStartY(), 
            button.getWidth(), button.getHeight());
    }

    public void setToolTip(Component text) {
        toolTip.clear();
        toolTip.add(text);
    }

    public void setIcon(RenderElement icon) {
        this.icon = icon;
    }
}
