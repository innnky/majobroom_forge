package com.rcell.majobroom.client.sound;

import com.rcell.majobroom.entity.BroomEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 扫帚飞行音效
 * 100%复刻原版鞘翅音效实现 {@link net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance}
 * 仅将player替换为broom，其余逻辑完全一致
 */
@OnlyIn(value=Dist.CLIENT)
public class BroomFlyingSound extends AbstractTickableSoundInstance {
    
    public static final int DELAY = 20;
    private final BroomEntity broom;
    private int time;
    
    public BroomFlyingSound(BroomEntity broom) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.broom = broom;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.01f;
    }
    
    @Override
    public void tick() {
        ++this.time;
        // 原版：!this.player.isRemoved() && (this.time <= 20 || this.player.isFallFlying())
        // 扫帚版：检查扫帚是否存活且有乘客
        if (!this.broom.isRemoved() && (this.time <= 20 || this.broom.getFirstPassenger() != null)) {
            this.x = (float)this.broom.getX();
            this.y = (float)this.broom.getY();
            this.z = (float)this.broom.getZ();
            float f = (float)this.broom.getDeltaMovement().lengthSqr();
            this.volume = (double)f >= 1.0E-7 ? Mth.clamp(f / 4.0f, 0.0f, 1.0f) : 0.0f;
            if (this.time < 20) {
                this.volume = 0.0f;
            } else if (this.time < 40) {
                this.volume *= (float)(this.time - 20) / 20.0f;
            }
            float f1 = 0.8f;
            this.pitch = this.volume > 0.8f ? 1.0f + (this.volume - 0.8f) : 1.0f;
            // this.volume = 0f;
        } else {
            this.stop();
        }
    }
}

