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
 * 扫帚输入控制网络包（完整的6方向输入）
 * 使用位标志压缩7个布尔值到1个字节
 */
public record BroomInputPayload(int entityId, byte flags) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<BroomInputPayload> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "broom_input"));
    
    // flags 位标志（7位）:
    // bit 0 (0x01): 左转 (A)
    // bit 1 (0x02): 右转 (D)
    // bit 2 (0x04): 前进 (W)
    // bit 3 (0x08): 后退 (S)
    // bit 4 (0x10): 上升 (Space/Jump)
    // bit 5 (0x20): 下降 (Ctrl)
    // bit 6 (0x40): 刹车 (Shift)
    
    public static final StreamCodec<ByteBuf, BroomInputPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        BroomInputPayload::entityId,
        ByteBufCodecs.BYTE,
        BroomInputPayload::flags,
        BroomInputPayload::new
    );
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(BroomInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            
            // 获取目标实体
            Entity target = player.level().getEntity(payload.entityId);
            if (!(target instanceof BroomEntity broom)) {
                return;
            }
            
            // 验证：玩家必须正在骑乘该扫帚
            Entity vehicle = player.getVehicle();
            if (vehicle == null || !vehicle.equals(broom)) {
                return;
            }
            
            // 解析所有输入标志
            boolean left = (payload.flags & 0x01) != 0;
            boolean right = (payload.flags & 0x02) != 0;
            boolean forward = (payload.flags & 0x04) != 0;
            boolean backward = (payload.flags & 0x08) != 0;
            boolean up = (payload.flags & 0x10) != 0;
            boolean down = (payload.flags & 0x20) != 0;
            boolean brake = (payload.flags & 0x40) != 0;
            
            // 应用所有输入到扫帚
            broom.setInput(left, right, forward, backward, up, down, brake);
        });
    }
}

