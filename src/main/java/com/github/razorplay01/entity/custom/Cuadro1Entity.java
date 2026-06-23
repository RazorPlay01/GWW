package com.github.razorplay01.entity.custom;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animation.AnimatableManager;

public class Cuadro1Entity extends BaseCuadroEntity {

    public Cuadro1Entity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    protected AABB makeBoundingBox() {
        double width = 2.0;
        double height = 2.0;
        double depth = 0.1;

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        float yaw = this.getYRot();

        double halfWidth = width / 2.0;
        double halfDepth = depth / 2.0;

        double cos = Math.cos(Math.toRadians(-yaw));
        double sin = Math.sin(Math.toRadians(-yaw));

        double dx = halfWidth * cos;
        double dz = halfWidth * sin;

        double pdx = halfDepth * sin;
        double pdz = -halfDepth * cos;

        return new AABB(
                x - dx - pdx,
                y,
                z - dz - pdz,
                x + dx + pdx,
                y + height,
                z + dz + pdz
        );
    }

    @Override
    protected void onBound(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999999, 3, false, false));
    }

    @Override
    protected void onUnbound(Player player) {
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    @Override
    protected boolean canBound() {
        return true;
    }
}