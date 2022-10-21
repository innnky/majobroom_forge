package com.innky.majobroom.item;

//import com.innky.majobroom.entity.MajoBroom;
//import com.innky.majobroom.registry.EntityTypeRegistry;
import com.innky.majobroom.entity.MajoBroom;
import com.innky.majobroom.registry.EntityTypeRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class BroomItem extends Item {
    public BroomItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemStack = playerIn.getItemInHand(handIn);

            BlockHitResult result = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.ANY);
            if (result.getType() == HitResult.Type.BLOCK ){
                BlockPos blockPos = result.getBlockPos();

                if(!worldIn.isClientSide){
                    MajoBroom broomEntity = new MajoBroom(EntityTypeRegistry.majoBroom.get(), worldIn);
                    broomEntity.setPos(blockPos.getX()+0.5, blockPos.getY()+1.5, blockPos.getZ()+0.5);
                    broomEntity.setControlMode(itemStack.getOrCreateTag().getBoolean("controlMode"));
                    worldIn.addFreshEntity(broomEntity);
                    itemStack.shrink(1);
                }

                worldIn.playSound(playerIn,playerIn.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 10F,1f);
                addParticle(worldIn,blockPos.getX()+0.5, blockPos.getY()+2.1, blockPos.getZ()+0.5, 30,2,1);
            }

        return InteractionResultHolder.success(itemStack);
    }



//    @Override
//    public boolean updateItemStackNBT(CompoundTag nbt) {
////        nbt.putFloat("aaa",10);
//
//        return true;
//    }

    public static void addParticle(Level world, double tx, double ty, double tz, int number, double width, double height){
        for (int i = 0; i<number; i++){
//            System.out.println(1);
            double x = Math.random()*width-0.5*width;
            double y = Math.random()*height-0.5*height;
            double z = Math.random()*width-0.5*width;
            world.addParticle(ParticleTypes.SMOKE,tx+x, ty+y, tz+z, 0,0,0);
        }
    }
}
