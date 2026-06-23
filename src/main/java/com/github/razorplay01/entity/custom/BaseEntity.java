package com.github.razorplay01.entity.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BaseEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected BaseEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setSilent(true);
        this.setNoAi(true);
        this.setPersistenceRequired();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public abstract void handleNormalInteract(Player player);

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (interactionHand.equals(InteractionHand.MAIN_HAND) && !this.level().isClientSide) {
            handleNormalInteract(player);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, interactionHand);
    }
}
