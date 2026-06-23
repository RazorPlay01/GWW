package com.github.razorplay01.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class PuertaAticoEntity extends BaseEntity {

    private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(
            PuertaAticoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIMATION_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation ANIMATION_OPEN = RawAnimation.begin().thenPlayAndHold("animation.open");

    public PuertaAticoEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_OPEN, false);
    }

    public boolean isOpen() {
        return this.entityData.get(IS_OPEN);
    }

    public void setOpen(boolean open) {
        this.entityData.set(IS_OPEN, open);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsOpen", isOpen());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOpen(tag.getBoolean("IsOpen"));
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "puerta_controller",
                0,
                state -> isOpen()
                        ? state.setAndContinue(ANIMATION_OPEN)
                        : state.setAndContinue(ANIMATION_IDLE)
        ));
    }

    @Override
    public void handleNormalInteract(Player player) {
        if (!player.level().isClientSide) {
            setOpen(!isOpen());
        }
    }

    // ==================== COLISIÓN SELECTIVA ====================
    @Override
    public boolean canBeCollidedWith() {
        return !isOpen();           // Permite pasar cuando está abierta
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !isOpen() && super.canCollideWith(entity);
    }

    @Override
    public void push(Entity entity) {
        // No empuja al jugador cuando está abierta
        if (!isOpen()) {
            super.push(entity);
        }
    }

    @Override
    protected void pushEntities() {
        if (!isOpen()) {
            super.pushEntities();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (IS_OPEN.equals(key)) {
            this.refreshDimensions();
        }
    }
}