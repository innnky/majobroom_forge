package com.innky.majobroom.mixin;

import com.innky.majobroom.armors.MajoWearableItem;
import com.innky.majobroom.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Final private ItemColors itemColors;
    @Inject(method = "createSearchTrees",at = @At(value = "HEAD"))
    private void populateSearchTreeManager(CallbackInfo ci) {
        try {
//            System.out.println("testmixinininin");
//            itemColors.register((stack, color) -> {
//                return color > 0 ? -1 : ((MajoWearableItem)stack.getItem()).getColor(stack);
//            }, ItemRegistry.itemMap.get("majo_hat").get());
        }catch (Throwable e){

        }

    }
}