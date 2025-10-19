package com.rcell.majobroom.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BroomTrailParticles {
    private BroomTrailParticles() {}

    private static int counter = 0;

    public static void spawn(Level level, Vec3 pos) {
        if (!level.isClientSide) return;
        
        // 直接启用粒子效果，不再检查配置
        // 使用默认的中等质量（interval=3）
        if (++counter % 3 != 0) return;

        double ox = (level.random.nextDouble() - 0.5D) * 0.5D;
        double oy = 0.2D + level.random.nextDouble() * 0.2D;
        double oz = (level.random.nextDouble() - 0.5D) * 0.5D;
        var clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) return;
        clientLevel.addParticle(ParticleTypes.CLOUD,
                pos.x + ox, pos.y + oy, pos.z + oz,
                0.0D, 0.02D, 0.0D);
    }
}


