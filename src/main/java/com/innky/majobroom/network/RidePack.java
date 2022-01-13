package com.innky.majobroom.network;

import com.innky.majobroom.entity.MajoBroom;
import com.innky.majobroom.registry.ItemRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RidePack {
    private final int eid;
    private final boolean ride;
    public RidePack(FriendlyByteBuf packetBuffer){
        ride = packetBuffer.readBoolean();
        eid = packetBuffer.readInt();

    }
    public RidePack(int eid, boolean ride){
        this.eid = eid;
        this.ride = ride;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.ride);
        buf.writeInt(this.eid);
    }
    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            Player playerEntity = ctx.get().getSender();
            if (playerEntity != null){

                MajoBroom broomEntity = (MajoBroom) playerEntity.level.getEntity(this.eid);
                if(broomEntity != null) {
                    if (ride) {
                        playerEntity.startRiding(broomEntity);

                    } else {
                        broomEntity.remove(Entity.RemovalReason.KILLED);
                        ItemStack itemStack = new ItemStack(ItemRegistry.broomItem.get());
                        itemStack.getOrCreateTag().putBoolean("controlMode",broomEntity.getControlMode());
                        if (!playerEntity.isCreative()) {
                            if (!playerEntity.getInventory().add(itemStack)){
                                broomEntity.spawnAtLocation(broomEntity.getDropItem());
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
