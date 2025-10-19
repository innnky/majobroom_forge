package com.rcell.majobroom.client.renderer.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.item.armor.MajoRobeItem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 魔女长袍的 GeckoLib 渲染器
 * 处理 dress 节点的特殊运动逻辑：
 * - dress 的旋转由左右腿的平均旋转决定
 * - 在骑乘状态下隐藏 sithide1 和 sithide2 节点
 */
public class MajoClothRenderer extends GeoArmorRenderer<MajoRobeItem> {
    public MajoClothRenderer() {
        super(new DefaultedItemGeoModel<>(
            ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "armor/majo_cloth")
        ));
    }

    /**
     * 在渲染前处理 dress 节点的特殊运动逻辑
     */
    @Override
    public void preRender(PoseStack poseStack, MajoRobeItem animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);

        // 处理 dress 节点的特殊运动
        GeoBone dressBone = this.model.getBone("dress").orElse(null);
        if (dressBone != null && this.baseModel != null) {
            ModelPart leftLeg = this.baseModel.leftLeg;
            ModelPart rightLeg = this.baseModel.rightLeg;

            // dress 的 X 轴旋转是左右腿旋转的平均值
            dressBone.setRotX(-(leftLeg.xRot + rightLeg.xRot) / 2.0f);
            
            // dress 的 Z 轴位置跟随左腿
            dressBone.setPosZ(leftLeg.z);
            

            // 获取当前实体并检查是否在骑乘
            Entity entity = getCurrentEntity();
            boolean isRiding = entity != null && entity.isPassenger();

            if (isRiding) {
                // 骑乘状态：固定旋转角度
                dressBone.setRotX(1.04f);
                
                // 隐藏 sithide1 和 sithide2 节点
                this.model.getBone("sithide1").ifPresent(bone -> bone.setHidden(true));
                this.model.getBone("sithide2").ifPresent(bone -> bone.setHidden(true));
            } else {
                // 非骑乘状态：显示 sithide 节点
                this.model.getBone("sithide1").ifPresent(bone -> bone.setHidden(false));
                this.model.getBone("sithide2").ifPresent(bone -> bone.setHidden(false));
            }
        }
    }
}
