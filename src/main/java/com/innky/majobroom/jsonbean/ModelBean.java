package com.innky.majobroom.jsonbean;

import com.google.gson.annotations.SerializedName;

public class ModelBean {

    /**
     * format_version : 1.10.0
     * geometry.model : 1
     */

    private String format_version;
    @SerializedName("geometry.model")
    private GeomtryBean model; // FIXME check this code

    public String getFormat_version() {
        return format_version;
    }

    public void setFormat_version(String format_version) {
        this.format_version = format_version;
    }

    public GeomtryBean getModel() {
        return model;
    }

    public void setModel(GeomtryBean model) {
        this.model = model;
    }
}
