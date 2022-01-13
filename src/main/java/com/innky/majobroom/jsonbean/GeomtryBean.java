package com.innky.majobroom.jsonbean;

import java.util.List;

public class GeomtryBean {

    /**
     * texturewidth : 16
     * textureheight : 16
     * visible_bounds_width : 4
     * visible_bounds_height : 4
     * visible_bounds_offset : [0,1,0]
     * bones : [{"name":"bone","pivot":[0,0,0],"rotation":[0,0,15.00004],"cubes":[{"origin":[-18.48889,-1.88229,2],"size":[4,3,2],"uv":[0,0]},{"origin":[1,2,-4],"size":[3,1,14],"uv":[0,0]}]},{"name":"bone3","parent":"bone","pivot":[0,0,0],"rotation":[35,0,-32.5],"cubes":[{"origin":[-3.6,-0.6,14],"size":[9.6,9.6,5],"uv":[0,0]},{"origin":[-3,0,17],"size":[8.7,8.7,5],"uv":[0,0]},{"origin":[-2.8,0.4,19],"size":[8.2,7.9,5],"uv":[0,0]},{"origin":[-16.03528,3.8637,1],"size":[22,1,4],"uv":[0,0]},{"origin":[-4.8637,-1.03528,-20],"size":[3.9,33.3,21],"uv":[0,0]}]},{"name":"bone4","parent":"bone3","pivot":[0,0,0],"rotation":[0,0,62.5],"cubes":[{"origin":[-3,0,1],"size":[3,1,5],"uv":[0,0]},{"origin":[-3,0,1],"size":[3,1,0],"uv":[0,0]}]},{"name":"bone2","pivot":[0,0,0],"cubes":[{"origin":[-3,6,1],"size":[3,1,18],"uv":[0,0]}]}]
     */

    private int texturewidth;
    private int textureheight;
    private float visible_bounds_width;
    private float visible_bounds_height;
    private List<Float> visible_bounds_offset;
    private List<BonesBean> bones;

    public int getTexturewidth() {
        return texturewidth;
    }

    public void setTexturewidth(int texturewidth) {
        this.texturewidth = texturewidth;
    }

    public int getTextureheight() {
        return textureheight;
    }

    public void setTextureheight(int textureheight) {
        this.textureheight = textureheight;
    }

    public float getVisible_bounds_width() {
        return visible_bounds_width;
    }

    public void setVisible_bounds_width(float visible_bounds_width) {
        this.visible_bounds_width = visible_bounds_width;
    }

    public float getVisible_bounds_height() {
        return visible_bounds_height;
    }

    public void setVisible_bounds_height(float visible_bounds_height) {
        this.visible_bounds_height = visible_bounds_height;
    }

    public List<Float> getVisible_bounds_offset() {
        return visible_bounds_offset;
    }

    public void setVisible_bounds_offset(List<Float> visible_bounds_offset) {
        this.visible_bounds_offset = visible_bounds_offset;
    }

    public List<BonesBean> getBones() {
        return bones;
    }

    public void setBones(List<BonesBean> bones) {
        this.bones = bones;
    }

    public static class BonesBean {
        /**
         * name : bone
         * pivot : [0,0,0]
         * rotation : [0,0,15.00004]
         * cubes : [{"origin":[-18.48889,-1.88229,2],"size":[4,3,2],"uv":[0,0]},{"origin":[1,2,-4],"size":[3,1,14],"uv":[0,0]}]
         * parent : bone
         */

        private String name;
        private String parent;
        private List<Float> pivot;
        private List<Float> rotation;
        private List<CubesBean> cubes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public List<Float> getPivot() {
            return pivot;
        }

        public void setPivot(List<Float> pivot) {
            this.pivot = pivot;
        }

        public List<Float> getRotation() {
            return rotation;
        }

        public void setRotation(List<Float> rotation) {
            this.rotation = rotation;
        }

        public List<CubesBean> getCubes() {
            return cubes;
        }

        public void setCubes(List<CubesBean> cubes) {
            this.cubes = cubes;
        }

        public static class CubesBean {
            private List<Float> origin;
            private List<Float> size;
            private List<Integer> uv;
            private float inflate;

            public float getInflate() {
                return inflate;
            }

            public void setInflate(float inflate) {
                this.inflate = inflate;
            }

            public List<Float> getOrigin() {
                return origin;
            }

            public void setOrigin(List<Float> origin) {
                this.origin = origin;
            }

            public List<Float> getSize() {
                return size;
            }

            public void setSize(List<Float> size) {
                this.size = size;
            }

            public List<Integer> getUv() {
                return uv;
            }

            public void setUv(List<Integer> uv) {
                this.uv = uv;
            }
        }
    }
}
