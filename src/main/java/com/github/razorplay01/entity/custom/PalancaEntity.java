package com.github.razorplay01.entity.custom;

import com.github.razorplay01.entity.BaseInteractiveEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PalancaEntity extends BaseInteractiveEntity {

    public PalancaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }
    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }
    @Override
    public void handleNormalInteract(Player player) {
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

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && !isBound()) {
            handleGravityAndMovement();
        }
    }

    private void handleGravityAndMovement() {
        this.setNoGravity(false);

        Vec3 motion = this.getDeltaMovement();

        if (!this.onGround()) {
            motion = motion.add(0, -0.08, 0);
            motion = motion.multiply(0.98, 0.98, 0.98);
        } else {
            motion = new Vec3(0, motion.y, 0);
        }

        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }
}
