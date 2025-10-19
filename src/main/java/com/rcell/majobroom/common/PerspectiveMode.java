package com.rcell.majobroom.common;

import net.minecraft.util.StringRepresentable;

/**
 * 视角模式枚举
 * 用于配置自动切换的视角类型
 */
public enum PerspectiveMode implements StringRepresentable {
    FIRST_PERSON("first_person", 0),
    SECOND_PERSON("second_person", 2),  // 前视（朝向玩家）
    THIRD_PERSON("third_person", 1);    // 后视（玩家背后）
    
    private final String name;
    private final int vanillaId;  // Minecraft 原版视角ID
    
    PerspectiveMode(String name, int vanillaId) {
        this.name = name;
        this.vanillaId = vanillaId;
    }
    
    @Override
    public String getSerializedName() {
        return name;
    }
    
    /**
     * 获取Minecraft原版的视角ID
     * 0 = 第一人称
     * 1 = 第三人称（后视）
     * 2 = 第二人称（前视）
     */
    public int getVanillaId() {
        return vanillaId;
    }
    
    /**
     * 获取下一个视角模式（循环）
     */
    public PerspectiveMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
    
    /**
     * 从序号获取视角模式
     */
    public static PerspectiveMode fromOrdinal(int ordinal) {
        PerspectiveMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return FIRST_PERSON;
        }
        return values[ordinal];
    }
    
    /**
     * 从名称获取视角模式
     */
    public static PerspectiveMode fromName(String name) {
        for (PerspectiveMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return FIRST_PERSON;
    }
}

