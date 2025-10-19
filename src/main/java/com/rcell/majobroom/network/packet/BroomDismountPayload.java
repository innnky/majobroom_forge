package com.rcell.majobroom.network.packet;

import com.rcell.majobroom.MajoBroom;
import com.rcell.majobroom.entity.BroomEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 扫帚下马数据包
 * 客户端 -> 服务端
 * 
 * 当玩家长按shift键超过1秒时，客户端发送此包请求下马
 */
public record BroomDismountPayload(int entityId) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<BroomDismountPayload> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "broom_dismount"));
    
    public static final StreamCodec<ByteBuf, BroomDismountPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        BroomDismountPayload::entityId,
        BroomDismountPayload::new
    );
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(BroomDismountPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            
            // 验证玩家确实在骑乘该扫帚
            Entity vehicle = player.getVehicle();
            if (vehicle == null) {
                return;
            }
            
            // 验证实体ID匹配
            Entity target = player.level().getEntity(payload.entityId);
            if (!(target instanceof BroomEntity broom) || !target.equals(vehicle)) {
                return;
            }
            
            // 设置允许下马标记，然后执行下马
            broom.setAllowDismount(true);
            player.stopRiding();
            // 下马后立即重置标记（以防下次意外）
            broom.setAllowDismount(false);
        });
    }
}

