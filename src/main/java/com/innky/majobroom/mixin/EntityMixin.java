package com.innky.majobroom.mixin;

import com.innky.majobroom.entity.MajoBroom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin {
//
//    @Shadow
//    @Nullable
//    public abstract Entity getVehicle();
//
//    @Inject(method = "isShiftKeyDown", at = @At(value = "HEAD"), cancellable = true)
//    private void isShiftKeyDown(CallbackInfoReturnable<Boolean> cir) {
//        if (this.getVehicle() instanceof MajoBroom) {
////            cir.setReturnValue(false);
//        }
//    }
//
//    @Inject(method = "isInWater", at = @At(value = "HEAD"), cancellable = true)
//    private void isInWater(CallbackInfoReturnable<Boolean> cir) {
//        if (this.getVehicle() instanceof MajoBroom) {
//            cir.setReturnValue(true);
//        }
//
//
//    }
}
