package com.github.razorplay01.entity;

import com.github.razorplay01.GWW;
import com.github.razorplay01.entity.custom.CannonBulletEntity;
import com.github.razorplay01.entity.custom.CannonEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<CannonEntity> CANNON = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "cannon"),
            EntityType.Builder.of(CannonEntity::new, MobCategory.MISC)
                    .sized(1.5f, 1.8f).build());

    public static final EntityType<CannonBulletEntity> CANNON_BULLET = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(GWW.MOD_ID, "cannon_bullet"),
            EntityType.Builder.of(CannonBulletEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f).build());

    public static void registerModEntities() {
        GWW.LOGGER.info("Registering Mod Entities for " + GWW.MOD_ID);
    }
}
