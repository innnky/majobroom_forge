package com.innky.majobroom.jsonbean;

public class ConfigBean {


    /**
     * Annotation : The speed value refers to the percentage of the default maximum speed
     * speed : 100
     */

    private String Annotation;
    private int speed;
    private boolean advancedMode;
    public String getAnnotation() {
        return Annotation;
    }

    public boolean isAdvancedMode() {
        return advancedMode;
    }

    public void setAdvancedMode(boolean advancedMode) {
        this.advancedMode = advancedMode;
    }

    public void setAnnotation(String Annotation) {
        this.Annotation = Annotation;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
