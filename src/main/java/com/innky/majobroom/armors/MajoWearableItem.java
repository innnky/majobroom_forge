package com.innky.majobroom.armors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class MajoWearableItem extends ArmorItem implements DyeableLeatherItem {
    public MajoWearableItem(ArmorMaterial materialIn, EquipmentSlot slot, Properties builderIn) {
        super(materialIn, slot, builderIn);
    }


    @Override
    public int getColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : 0xdda3c7;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {
//        player.addPotionEffect(new EffectInstance(Effects.SPEED,60,3));
    }


    @Override
    public @NotNull Object getRenderPropertiesInternal() {

        return new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return Modelinit.modelMap.get(itemStack.getDescriptionId().substring(15));
            }
        };
    }


    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (type!=null){
            ResourceLocation resourceLocation = new ResourceLocation("majobroom","jsonmodels/textures/"+stack.getDescriptionId().substring(15)+"_overlay.png");
            if(Minecraft.getInstance().getResourceManager().getResource(resourceLocation).isPresent()){
                return "majobroom:jsonmodels/textures/"+stack.getDescriptionId().substring(15)+"_overlay.png";
            }else {
                return "majobroom:jsonmodels/textures/"+stack.getDescriptionId().substring(15)+".png";
            }
        }else {
            return "majobroom:jsonmodels/textures/"+stack.getDescriptionId().substring(15)+".png";
        }

    }

}
