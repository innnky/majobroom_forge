package com.rcell.majobroom.client.gui.texture;

import com.rcell.majobroom.client.gui.base.RenderElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * GUI 图标管理枚举
 * 扫帚配置界面使用的图标
 */
public enum GuiIcons implements RenderElement {
    // icons.png 中每个图标 16x16
    // 坐标使用 (列, 行) * 16 计算
    
    // 配置界面图标（第1行 y=0）
    I_SIDEWAYS_SITTING(0, 0),    // (0, 0) * 16 - 侧坐姿势
    I_PERSPECTIVE_FIRST(1, 0),   // (1, 0) * 16 - 第一人称
    I_PERSPECTIVE_SECOND(2, 0),  // (2, 0) * 16 - 第二人称（前视）
    I_PERSPECTIVE_THIRD(3, 0),   // (3, 0) * 16 - 第三人称（后视）
    I_AUTO_HOVER(4, 0),          // (4, 0) * 16 - 自动悬浮
    I_CONFIRM(5, 0);             // (5, 0) * 16 - 确认按钮
    
    public static final ResourceLocation ICON_ATLAS = 
        ResourceLocation.fromNamespaceAndPath("majobroom", "textures/gui/icons.png");
    public static final int ICON_SIZE = 16;
    public static final int ICON_ATLAS_SIZE = 256;

    private final int iconX;
    private final int iconY;

    GuiIcons(int x, int y) {
        // 与 Create 的 AllIcons 相同：坐标 * 16
        this.iconX = x * 16;
        this.iconY = y * 16;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(ICON_ATLAS, x, y, iconX, iconY, ICON_SIZE, ICON_SIZE);
    }

    public int getIconX() {
        return iconX;
    }

    public int getIconY() {
        return iconY;
    }
}

