package com.rcell.majobroom.client.gui.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * GUI 纹理管理枚举
 * 参考 Create 的 AllGuiTextures 实现
 */
public enum GuiTextures {
    // 背景纹理
    BROOM_CONFIG("broom_config.png", 190, 151),
    
    // 按钮纹理 (来自 Create 的 widgets.png)
    BUTTON("widgets.png", 0, 0, 18, 18),
    BUTTON_HOVER("widgets.png", 18, 0, 18, 18),
    BUTTON_DOWN("widgets.png", 36, 0, 18, 18),
    BUTTON_GREEN("widgets.png", 72, 0, 18, 18),
    BUTTON_DISABLED("widgets.png", 90, 0, 18, 18),
    
    // 滑动条纹理 (来自 Create 的 value_settings.png)
    VALUE_SETTINGS_MILESTONE("value_settings.png", 0, 0, 7, 8),
    VALUE_SETTINGS_BAR("value_settings.png", 7, 0, 249, 8),
    VALUE_SETTINGS_BAR_BG("value_settings.png", 75, 9, 1, 1),
    VALUE_SETTINGS_CURSOR_LEFT("value_settings.png", 0, 9, 3, 14),
    VALUE_SETTINGS_CURSOR("value_settings.png", 4, 9, 56, 14),
    VALUE_SETTINGS_CURSOR_RIGHT("value_settings.png", 61, 9, 3, 14),
    ;

    public static final int TITLE_FONT_COLOR = 0x592424; // 蓝灰色   0x592424 深红棕色
    public static final int CONTENT_FONT_COLOR = 0xb8b8b8; // 蓝灰色   0x592424 深红棕色
    public static final String TEXTURES_PATH = "textures/gui/";

    private final ResourceLocation location;
    private final int width, height;
    private final int startX, startY;

    /**
     * 完整纹理构造（用于背景）
     */
    GuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    /**
     * 纹理图集构造（用于 widgets）
     */
    GuiTextures(String location, int startX, int startY, int width, int height) {
        this.location = ResourceLocation.fromNamespaceAndPath("majobroom", TEXTURES_PATH + location);
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    /**
     * 绑定纹理
     */
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    /**
     * 渲染纹理
     */
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    /**
     * 渲染指定区域的纹理
     */
    public void render(GuiGraphics graphics, int x, int y, int startX, int startY, int width, int height) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    /**
     * 裁剪渲染（从纹理的startX, startY开始，渲染指定宽度和高度）
     */
    public void renderCropped(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    /**
     * 拉伸渲染（将1x1或小纹理拉伸到指定宽度和高度）
     */
    public void renderStretched(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.blit(location, x, y, width, height, startX, startY, this.width, this.height, 256, 256);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }
}

