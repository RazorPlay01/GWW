package com.github.razorplay01.entity.client;

import com.github.razorplay01.entity.attribute.ModAttributes;
import com.github.razorplay01.entity.custom.Cuadro1Entity;
import com.github.razorplay01.entity.custom.RejaDuctoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.FastBoneFilterGeoLayer;

import java.util.List;

public class RejaDuctoEntityRenderer extends GeoEntityRenderer<RejaDuctoEntity> {
    public RejaDuctoEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new RejaDuctoEntityModel());
    }
}
