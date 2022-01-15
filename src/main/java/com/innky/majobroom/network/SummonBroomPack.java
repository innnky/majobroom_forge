package com.innky.majobroom.network;

import com.innky.majobroom.entity.MajoBroom;
import com.innky.majobroom.registry.EntityTypeRegistry;
import com.innky.majobroom.registry.ItemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SummonBroomPack {
    public SummonBroomPack(FriendlyByteBuf packetBuffer){


    }
    public SummonBroomPack(){
    }

    public void toBytes(FriendlyByteBuf buf) {

    }
    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            Player playerEntity = ctx.get().getSender();
            if (playerEntity != null ){
                if (!playerEntity.isPassenger()){
                    for (ItemStack item:playerEntity.getInventory().items) {
                        if (item.is(ItemRegistry.broomItem.get()) || playerEntity.isCreative()){
                            summonBroom(playerEntity,item);
                            break;
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
    private void summonBroom(Player playerEntity,ItemStack itemStack){
        MajoBroom broomEntity = new MajoBroom(EntityTypeRegistry.majoBroom.get(), playerEntity.level);
        broomEntity.setPos(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
        broomEntity.setControlMode(itemStack.getOrCreateTag().getBoolean("controlMode"));
        playerEntity.level.addFreshEntity(broomEntity);
        if (!playerEntity.isCreative()) {
            itemStack.shrink(1);
        }
        playerEntity.startRiding(broomEntity);

    }
}
