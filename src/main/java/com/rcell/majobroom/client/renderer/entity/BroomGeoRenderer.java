package com.rcell.majobroom.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.rcell.majobroom.entity.BroomEntity;
import com.rcell.majobroom.client.renderer.model.BroomGeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 扫帚实体渲染器
 * 使用 GeckoLib 渲染模型
 * 重写 applyRotations 来处理非 LivingEntity 的旋转
 */
public class BroomGeoRenderer extends GeoEntityRenderer<BroomEntity> {
    public BroomGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new BroomGeoModel());
        this.shadowRadius = 0.3F;
    }
    
    /**
     * 在渲染前应用浮动偏移
     * 这是 GeckoLib 推荐的方式，不会干扰实体的旋转处理
     */
    @Override
    public void preRender(PoseStack poseStack, BroomEntity animatable, BakedGeoModel model, 
                         MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                         float partialTick, int packedLight, int packedOverlay, 
                         float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, 
                       partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        
        // 应用服务端计算的浮动偏移（带插值）
        float floatOffset = animatable.getInterpolatedFloatOffset(partialTick);
        poseStack.translate(0.0D, floatOffset, 0.0D);
    }
    
    /**
     * 重写旋转应用逻辑以支持非 LivingEntity
     * 因为 BroomEntity 不是 LivingEntity，默认的 GeckoLib 逻辑会导致 lerpBodyRot = 0
     * 我们需要手动使用实体的 yRot 来应用旋转
     */
    @Override
    protected void applyRotations(BroomEntity animatable, PoseStack poseStack, float ageInTicks, 
                                  float rotationYaw, float partialTick) {
        // 使用实体的实际旋转角度（带插值）
        float entityYaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        
        // 应用 Y 轴旋转（180度是因为模型默认朝向）
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
    }
}



