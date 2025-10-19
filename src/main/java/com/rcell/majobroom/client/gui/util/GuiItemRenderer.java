package com.rcell.majobroom.client.gui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * GUI 物品渲染工具
 * 参考 catnip 的 GuiGameElement.GuiItemRenderBuilder
 * 用于在 GUI 中渲染可缩放的物品图标
 */
public class GuiItemRenderer {
    
    /**
     * 在 GUI 中渲染物品，支持位置和缩放
     * 
     * @param graphics 渲染上下文
     * @param stack 物品栈
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标（深度）
     * @param scale 缩放比例
     */
    public static void renderItemAt(GuiGraphics graphics, ItemStack stack, float x, float y, float z, float scale) {
        PoseStack poseStack = graphics.pose();
        
        // 开始渲染准备（prepareMatrix）
        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Lighting.setupFor3DItems();
        
        // 应用变换（transformMatrix）
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        // 重要：Y轴翻转，与 GuiGameElement 的 UIRenderHelper.flipForGuiRender 一致
        poseStack.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
        
        // 渲染物品
        renderItemIntoGUI(poseStack, stack);
        
        // 清理
        poseStack.popPose();
    }
    
    /**
     * 内部渲染方法
     * 完全参照 GuiGameElement.GuiItemRenderBuilder.renderItemIntoGUI
     */
    private static void renderItemIntoGUI(PoseStack poseStack, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();
        BakedModel bakedModel = renderer.getModel(stack, null, null, 0);
        
        mc.getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        poseStack.pushPose();
        poseStack.translate(0, 0, 100.0F);
        poseStack.translate(8.0F, -8.0F, 0.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);
        
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flatLighting = !bakedModel.usesBlockLight();
        if (flatLighting) {
            Lighting.setupForFlatItems();
        }
        
        renderer.render(stack, ItemDisplayContext.GUI, false, poseStack, buffer, 
                       LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedModel);
        
        RenderSystem.disableDepthTest();
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        
        if (flatLighting) {
            Lighting.setupFor3DItems();
        }
        
        poseStack.popPose();
    }
}

