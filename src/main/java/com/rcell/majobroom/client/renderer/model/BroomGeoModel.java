package com.rcell.majobroom.client.renderer.model;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.entity.BroomEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 扫帚的 GeckoLib 模型
 * 定义模型和纹理资源的位置（不使用动画系统）
 */
public class BroomGeoModel extends GeoModel<BroomEntity> {
    // 使用 GeckoLib 标准路径
    private static final ResourceLocation MODEL = 
        ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "geo/entity/broom.geo.json");
    
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "textures/entity/broom.png");

    @Override
    public ResourceLocation getModelResource(BroomEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BroomEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BroomEntity animatable) {
        // 不使用动画文件 - 浮动效果由服务端计算并在渲染器中应用
        return null;
    }
}


