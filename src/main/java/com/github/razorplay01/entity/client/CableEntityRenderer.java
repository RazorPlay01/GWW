package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.custom.CableEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.Collections;

public class CableEntityRenderer extends GeoEntityRenderer<CableEntity> {
    public CableEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CableEntityModel());
        this.addRenderLayer(new FastBoneFilterGeoLayer<>(this,
                () -> Collections.singletonList("group"),
                (bone, entity, partialTick) -> bone.setHidden(!entity.isActive())));
    }
}
