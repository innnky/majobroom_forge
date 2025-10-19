package com.rcell.majobroom.client.gui.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * 基础屏幕类
 * 替代 catnip 的 AbstractSimiScreen
 * 提供窗口布局、控件管理、渲染流程等基础功能
 */
public abstract class BaseScreen extends Screen {
    
    protected int windowWidth, windowHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;

    protected BaseScreen(Component title) {
        super(title);
    }

    protected BaseScreen() {
        this(CommonComponents.EMPTY);
    }

    /**
     * 设置窗口大小（必须在 init() 之前调用）
     */
    protected void setWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    /**
     * 设置窗口偏移（必须在 init() 之前调用）
     */
    protected void setWindowOffset(int xOffset, int yOffset) {
        windowXOffset = xOffset;
        windowYOffset = yOffset;
    }

    @Override
    protected void init() {
        // 计算窗口居中位置
        guiLeft = (width - windowWidth) / 2;
        guiTop = (height - windowHeight) / 2;
        // 应用偏移
        guiLeft += windowXOffset;
        guiTop += windowYOffset;
    }

    /**
     * 批量添加可渲染控件
     */
    @SuppressWarnings("unchecked")
    protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(W... widgets) {
        for (W widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    /**
     * 批量添加可渲染控件
     */
    protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(Collection<W> widgets) {
        for (W widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    /**
     * 批量移除控件
     */
    protected void removeWidgets(@Nonnull GuiEventListener... widgets) {
        for (GuiEventListener widget : widgets) {
            removeWidget(widget);
        }
    }

    /**
     * 批量移除控件
     */
    protected void removeWidgets(Collection<? extends GuiEventListener> widgets) {
        for (GuiEventListener widget : widgets) {
            removeWidget(widget);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染背景
        renderBackground(graphics);
        
        // 渲染窗口内容（由子类实现）
        renderWindow(graphics, mouseX, mouseY, partialTicks);
        
        // 渲染控件
        super.render(graphics, mouseX, mouseY, partialTicks);
        
        // 渲染工具提示
        renderWindowForeground(graphics, mouseX, mouseY, partialTicks);
    }

    /**
     * 渲染窗口内容（由子类实现）
     */
    protected abstract void renderWindow(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    /**
     * 渲染前景层（工具提示等）
     */
    protected void renderWindowForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // 遍历所有控件，渲染工具提示
        for (Renderable widget : renderables) {
            if (widget instanceof BaseWidget baseWidget && baseWidget.isHovered() && baseWidget.visible) {
                if (!baseWidget.getToolTip().isEmpty()) {
                    graphics.renderComponentTooltip(font, baseWidget.getToolTip(), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean keyPressed = super.keyPressed(keyCode, scanCode, modifiers);
        if (keyPressed || getFocused() != null)
            return keyPressed;

        // 允许用 E 键关闭界面（背包键）
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

