package com.github.razorplay01.entity.client;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.CannonBulletEntity;
import com.github.razorplay01.entity.custom.CannonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CannonBulletEntityModel extends GeoModel<CannonBulletEntity> {
    @Override
    public ResourceLocation getModelResource(CannonBulletEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "geo/cannon_bullet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CannonBulletEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "textures/entity/cannon_bullet.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CannonBulletEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "animations/cannon_bullet.animation.json");
    }
}
