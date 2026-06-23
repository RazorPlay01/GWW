package com.github.razorplay01.entity.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class UblablaEntity extends PathfinderMob implements GeoEntity {
    protected static final RawAnimation IDLE = RawAnimation.begin().then("animation.idle", Animation.LoopType.LOOP);
    protected static final RawAnimation WALK = RawAnimation.begin().then("animation.walk", Animation.LoopType.LOOP);
    protected static final RawAnimation CHECK = RawAnimation.begin().thenPlay("animation.check");
    protected static final RawAnimation ATACK = RawAnimation.begin().thenPlay("animation.ataque");
    protected static final RawAnimation CHASE = RawAnimation.begin().thenPlay("animation.chase");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public UblablaEntity(EntityType<? extends UblablaEntity> type, Level level) {
        super(type, level);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle_controller", 0, this::animController));
    }

    protected <E extends UblablaEntity> PlayState animController(final AnimationState<E> state) {
        if (state.isMoving()) {
            state.getController().setAnimation(WALK);
        }

        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
