package com.innky.majobroom.entity.model;

import com.innky.majobroom.entity.MajoBroom;
import com.innky.majobroom.jsonbean.GeomtryBean;
import com.innky.majobroom.utills.ModelJsonReader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class JsonBroomModel extends EntityModel<MajoBroom> {
    public final ModelPart base;
    private final Map<String, PartDefinition> bones = new HashMap();
    private final HashMap<String, GeomtryBean.BonesBean> bonesBean = new HashMap();

    public JsonBroomModel(String path) {
        this.base = getTexturedModelData(path).bakeRoot();

    }

    private float convertOrigin(GeomtryBean.BonesBean bones, GeomtryBean.BonesBean.CubesBean cubes, int index) {
        if (index == 1) {
            return bones.getPivot().get(index) - cubes.getOrigin().get(index) - cubes.getSize().get(index);
        } else {
            return cubes.getOrigin().get(index) - bones.getPivot().get(index);
        }
    }



    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        matrixStack.pushPose();
        matrixStack.mulPoseMatrix(new Matrix4f(Vector3f.ZP.rotationDegrees(180)));
        matrixStack.translate(0,-1.5,0);
        base.render(matrixStack,buffer,packedLight,packedOverlay,red,green,blue,alpha);
        matrixStack.popPose();
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    @Override
    public void setupAnim(MajoBroom entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    public LayerDefinition getTexturedModelData(String path) {
        GeomtryBean model =  ModelJsonReader.readJson(path);


        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
//        modelPartData.addChild("sdasdsads", ModelPartBuilder.create().uv(0, 0).cuboid(-6F, 12F, -6F, 24F, 24F, 24F), ModelTransform.pivot(0F, 100F, 5F));
//
//        var another_cube = ModelPartBuilder.create().uv(0, 0).cuboid(-6F, 12F, -6F, 12F, 12F, 12F);
//        modelPartData.getChild("sdasdsads").addChild("aaa",another_cube,ModelTransform.pivot(0F, 1F, 1F));


        if (model != null) {

            for (GeomtryBean.BonesBean bone : model.getBones()) {
                CubeListBuilder newBone = CubeListBuilder.create();
                float x=0,y=0,z=0;
                if (bone.getRotation() != null) {
                    x = 0.017453F * (Float) bone.getRotation().get(0);
                    y = 0.017453F * (Float) bone.getRotation().get(1);
                    z = 0.017453F * (Float) bone.getRotation().get(2);
                }

                if (bone.getCubes() != null) {
                    for (GeomtryBean.BonesBean.CubesBean cube : bone.getCubes()) {
                        newBone.texOffs((Integer) cube.getUv().get(0), (Integer) cube.getUv().get(1)).addBox(this.convertOrigin(bone, cube, 0), this.convertOrigin(bone, cube, 1), this.convertOrigin(bone, cube, 2), (Float) cube.getSize().get(0), (Float) cube.getSize().get(1), (Float) cube.getSize().get(2),new CubeDeformation(cube.getInflate()));
                    }
                }

                PartDefinition nnnn = null;

                if (bone.getParent() != null) {
                    GeomtryBean.BonesBean parent = (GeomtryBean.BonesBean) this.bonesBean.get(bone.getParent());
                    ((PartDefinition) this.bones.get(bone.getParent())).addOrReplaceChild(bone.getName(),newBone,PartPose.offsetAndRotation((Float) bone.getPivot().get(0) - (Float) parent.getPivot().get(0), (Float) parent.getPivot().get(1) - (Float) bone.getPivot().get(1), (Float) bone.getPivot().get(2) - (Float) parent.getPivot().get(2),x,y,z));
                    nnnn = ((PartDefinition) this.bones.get(bone.getParent())).getChild(bone.getName());
                } else {
                    modelPartData.addOrReplaceChild(bone.getName(),newBone, PartPose.offsetAndRotation((Float) bone.getPivot().get(0), 24.0F - (Float) bone.getPivot().get(1), (Float) bone.getPivot().get(2),x,y,z));
                    nnnn = modelPartData.getChild(bone.getName());
                }

                this.bones.put(bone.getName(), nnnn);
                this.bonesBean.put(bone.getName(), bone);
            }
        }

        return LayerDefinition.create(modelData, model.getTexturewidth(), model.getTextureheight());
    }


}
