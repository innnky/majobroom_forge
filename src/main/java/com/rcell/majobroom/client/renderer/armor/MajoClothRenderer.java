package com.rcell.majobroom.client.renderer.armor;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.item.armor.MajoRobeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 魔女长袍的 GeckoLib 渲染器。
 * 新模型已经移除了旧的裙摆骨骼，不再需要额外的骨骼修正逻辑。
 */
public class MajoClothRenderer extends GeoArmorRenderer<MajoRobeItem> {
    public MajoClothRenderer() {
        super(new DefaultedItemGeoModel<>(
            ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "armor/majo_cloth")
        ));
    }
}
