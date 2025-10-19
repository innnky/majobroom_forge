package com.rcell.majobroom.client.renderer.armor;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.item.armor.MajoHatItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 魔女帽子的 GeckoLib 渲染器
 */
public class MajoHatRenderer extends GeoArmorRenderer<MajoHatItem> {
    public MajoHatRenderer() {
        super(new DefaultedItemGeoModel<>(
            ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "armor/majo_hat")
        ));
    }
}
