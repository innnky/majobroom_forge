package com.rcell.majobroom.item.armor;

import com.rcell.majobroom.MajoBroom;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * 魔女装备材质
 * NeoForge 1.21.1: ArmorMaterial从接口改为类，需要直接创建实例
 */
public class MajoArmorMaterials {
    
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = 
        DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, MajoBroom.MODID);
    
    /**
     * 魔女布料材质 - 轻便但防御较低的魔法布料
     */
    public static final Holder<ArmorMaterial> MAJO_CLOTH = ARMOR_MATERIALS.register("majo_cloth", 
        () -> new ArmorMaterial(
            // 防御值映射
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);       // 靴子: 1点防御
                map.put(ArmorItem.Type.LEGGINGS, 2);    // 护腿: 2点防御
                map.put(ArmorItem.Type.CHESTPLATE, 3);  // 胸甲: 3点防御
                map.put(ArmorItem.Type.HELMET, 1);      // 头盔: 1点防御
                map.put(ArmorItem.Type.BODY, 3);        // 身体: 3点防御
            }),
            // 魔咒加成 - 较高的魔咒加成，适合魔法装备
            15,
            // 装备音效
            SoundEvents.ARMOR_EQUIP_LEATHER,
            // 修复材料 - 使用羊毛修复
            () -> Ingredient.of(Items.WHITE_WOOL),
            // 材质层
            List.of(
                new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(MajoBroom.MODID, "majo_cloth")
                )
            ),
            // 韧性值 - 布料无韧性
            0.0F,
            // 击退抗性 - 布料无击退抗性
            0.0F
        )
    );
}
