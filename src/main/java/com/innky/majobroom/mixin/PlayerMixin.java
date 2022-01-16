package com.innky.majobroom.mixin;

import com.innky.majobroom.entity.MajoBroom;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {


    @Shadow public abstract Abilities getAbilities();

    protected PlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

//    @Inject(method = "tick",  at = @At(value = "HEAD"))
//    private void tick(CallbackInfo ci){
//        this.getAbilities().flying = this.getVehicle() instanceof MajoBroom;
//    }
    @Inject(method = "getStandingEyeHeight",  at = @At(value = "HEAD"),cancellable = true)
    private void getStandingEyeHeight(Pose p_36259_, EntityDimensions p_36260_, CallbackInfoReturnable<Float> cir){
        if (this.getVehicle() instanceof MajoBroom){
            cir.setReturnValue(1.62F);
        }

    }



}
