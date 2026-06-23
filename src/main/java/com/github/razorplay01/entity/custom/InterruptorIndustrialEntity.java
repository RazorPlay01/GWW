package com.github.razorplay01.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class InterruptorIndustrialEntity extends BaseEntity {

    // ==================== ESTADOS ====================
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(
            InterruptorIndustrialEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIMATION_ON = RawAnimation.begin().thenPlayAndHold("On");
    private static final RawAnimation ANIMATION_OFF = RawAnimation.begin().thenPlayAndHold("Off");

    public InterruptorIndustrialEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, 0); // 0 = OFF, 1 = ON
    }

    // Getters y Setters
    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setState(int state) {
        this.entityData.set(STATE, state == 1 ? 1 : 0);
    }

    public boolean isOn() {
        return getState() == 1;
    }

    // ==================== PERSISTENCIA ====================
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", getState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(tag.getInt("State"));
    }

    public static AttributeSupplier.Builder setAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, Double.POSITIVE_INFINITY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "interruptor_controller",
                0,
                state -> {
                    // Cambia la animación según el estado
                    if (isOn()) {
                        return state.setAndContinue(ANIMATION_ON);
                    } else {
                        return state.setAndContinue(ANIMATION_OFF);
                    }
                }
        ));
    }

    @Override
    public void handleNormalInteract(Player player) {
        if (!player.level().isClientSide) {
            // Clic derecho alterna entre ON y OFF
            setState(isOn() ? 0 : 1);
        }
    }
}