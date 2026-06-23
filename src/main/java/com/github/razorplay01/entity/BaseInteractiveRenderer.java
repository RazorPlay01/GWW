package com.github.razorplay01.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BaseInteractiveRenderer extends EntityRenderer<BaseInteractiveEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("tumod", "textures/entity/base_interactive.png");

    public BaseInteractiveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BaseInteractiveEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // Ajustar posición vertical
        poseStack.translate(0.0, 0.5, 0.0);

        // Escala
        float scale = entity.isBound() ? 0.8F : 0.6F;
        poseStack.scale(scale, scale, scale);

        // Rotación continua
        float rotation = (entity.tickCount + partialTicks) * 3.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Renderizar cubo simple
        renderCube(poseStack, buffer, packedLight, entity.isBound());

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderCube(PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean bound) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(null)));

        PoseStack.Pose pose = poseStack.last();

        // Color basado en estado
        float r = bound ? 0.8F : 0.5F;
        float g = bound ? 0.3F : 0.8F;
        float b = bound ? 0.5F : 1.0F;
        float a = 1.0F;

        float size = 0.3F;

        // Cara frontal (Z+)
        addQuad(vertexConsumer, pose, packedLight,
                -size, -size, size,
                size, -size, size,
                size, size, size,
                -size, size, size,
                0, 0, 1, r, g, b, a);

        // Cara trasera (Z-)
        addQuad(vertexConsumer, pose, packedLight,
                size, -size, -size,
                -size, -size, -size,
                -size, size, -size,
                size, size, -size,
                0, 0, -1, r, g, b, a);

        // Cara superior (Y+)
        addQuad(vertexConsumer, pose, packedLight,
                -size, size, size,
                size, size, size,
                size, size, -size,
                -size, size, -size,
                0, 1, 0, r, g, b, a);

        // Cara inferior (Y-)
        addQuad(vertexConsumer, pose, packedLight,
                -size, -size, -size,
                size, -size, -size,
                size, -size, size,
                -size, -size, size,
                0, -1, 0, r, g, b, a);

        // Cara derecha (X+)
        addQuad(vertexConsumer, pose, packedLight,
                size, -size, size,
                size, -size, -size,
                size, size, -size,
                size, size, size,
                1, 0, 0, r, g, b, a);

        // Cara izquierda (X-)
        addQuad(vertexConsumer, pose, packedLight,
                -size, -size, -size,
                -size, -size, size,
                -size, size, size,
                -size, size, -size,
                -1, 0, 0, r, g, b, a);
    }

    private void addQuad(VertexConsumer consumer, PoseStack.Pose pose, int light,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float nx, float ny, float nz,
                         float r, float g, float b, float a) {

        addVertex(consumer, pose, light, x1, y1, z1, 0, 0, nx, ny, nz, r, g, b, a);
        addVertex(consumer, pose, light, x2, y2, z2, 1, 0, nx, ny, nz, r, g, b, a);
        addVertex(consumer, pose, light, x3, y3, z3, 1, 1, nx, ny, nz, r, g, b, a);
        addVertex(consumer, pose, light, x4, y4, z4, 0, 1, nx, ny, nz, r, g, b, a);
    }

    private void addVertex(VertexConsumer consumer, PoseStack.Pose pose, int light,
                           float x, float y, float z, float u, float v,
                           float nx, float ny, float nz,
                           float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    public ResourceLocation getTextureLocation(BaseInteractiveEntity entity) {
        return TEXTURE;
    }
}