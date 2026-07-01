package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.CannonEntity;
import com.github.razorplay01.entity.custom.UblablaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class UblablaEntityRenderer extends GeoEntityRenderer<UblablaEntity> {
    public UblablaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new UblablaEntityModel());
    }

   /* @Override
    public void render(UblablaEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Component debug =
                switch (entity.getState()) {
                    case 0 -> Component.literal(entity.getState() + "- STATE_PATROL");
                    case 1 -> Component.literal(entity.getState() + "- STATE_ALERT");
                    case 2 -> Component.literal(entity.getState() + "- STATE_INVESTIGATING");
                    case 3 -> Component.literal(entity.getState() + "- STATE_CHASING");
                    case 4 -> Component.literal(entity.getState() + "- STATE_ATTACKING");
                    default -> Component.literal(entity.getState() + "- DEFAULT");
                };
        Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
        poseStack.pushPose();
        poseStack.translate(vec3.x, vec3.y + (double)0.5F, vec3.z);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = this.getFont();
        float h = -font.width(debug) / 2f;
        font.drawInBatch(debug, h, 0, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
*/
}
