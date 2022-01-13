package com.innky.majobroom.sound;

import com.innky.majobroom.entity.MajoBroom;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
//import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)

public class FlyingSound extends AbstractTickableSoundInstance {
    private final MajoBroom broom;
    private int time;


    public FlyingSound(MajoBroom player) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS);
        this.broom = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.1F;
    }

    @Override
    public void tick() {
        ++this.time;
//        System.out.println(time);
        if (this.broom.isAlive() && broom.hasPassenger) {
            this.x = (double)((float)this.broom.getX());
            this.y = (double)((float)this.broom.getY());
            this.z = (double)((float)this.broom.getZ());
            float f = (float)this.broom.getDeltaMovement().lengthSqr();
            if ((double)f >= 1.0E-7D) {
                this.volume = Mth.clamp(f / 4.0F, 0.0F, 1.0F);
            } else {
                this.volume = 0.0F;
            }

            if (this.time < 20) {
                this.volume = 0.0F;
            } else if (this.time < 40) {
                this.volume = (float)((double)this.volume * ((double)(this.time - 20) / 20.0D));
            }

            float f1 = 0.8F;
            if (this.volume > 0.8F) {
                this.pitch = 1.0F + (this.volume - 0.8F);
            } else {
                this.pitch = 1.0F;
            }

        } else {
//            System.out.println("stoped");
            this.stop();
        }


    }
}
